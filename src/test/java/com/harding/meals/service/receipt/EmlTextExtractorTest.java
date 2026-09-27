package com.harding.meals.service.receipt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for .eml text extraction. The fixture mirrors the evidenced
 * Tesco order email shape: single text/html part, quoted-printable encoded.
 */
class EmlTextExtractorTest {

    private final EmlTextExtractor extractor = new EmlTextExtractor();

    private static final String TESCO_STYLE_EML = """
            From: Tesco <no-reply@mail.tesco.com>
            To: someone@example.com
            Subject: Receipt for your tesco.com order 1234-5678-901 today
            MIME-Version: 1.0
            Content-Type: text/html; charset=UTF-8
            Content-Transfer-Encoding: quoted-printable

            <html><head><style>.x{color:red}</style></head><body>
            <h1>Your order summary - 1234-5678-901</h1>
            <table>
            <tr><td>Qty</td><td>Product</td><td>Unit price</td><td>Total</td></tr>
            <tr><td>1</td><td>Tesco Carrots Loose 0.348KG</td><td>=C2=A30.69</td><td>=C2=A30.24</td></tr>
            <tr><td>2</td><td>Tesco Babyleaf Salad 90G</td><td>=C2=A31.00</td><td>=C2=A32.00</td></tr>
            </table>
            <p>Total: =C2=A310.86</p>
            </body></html>
            """;

    @Test
    void extract_decodesQuotedPrintableHtmlToLineOrientedText() {
        String text = extractor.extract(TESCO_STYLE_EML);

        assertTrue(text.contains("Tesco Carrots Loose 0.348KG"));
        assertTrue(text.contains("£0.69"), "Quoted-printable £ signs should be decoded");
        assertTrue(text.contains("Total: £10.86"));
        assertTrue(text.lines().count() > 3, "Table rows should become separate lines");
    }

    @Test
    void extract_stripsStyleContent() {
        String text = extractor.extract(TESCO_STYLE_EML);
        assertTrue(!text.contains("color:red"));
    }

    @Test
    void extract_rejectsContentThatYieldsNoText() {
        // Headerless content parses as all-headers with an empty body; an empty
        // extraction must fail rather than silently feed nothing downstream
        assertThrows(IllegalArgumentException.class, () -> extractor.extract("just some pasted text"));
    }
}
