package com.harding.meals.service.receipt;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Extracts readable text from a raw .eml email source. Grocer order emails
 * (evidenced: Tesco) carry a single HTML body part; the visible text is what
 * the LLM receipt parser consumes.
 */
@Component
public class EmlTextExtractor {

    public String extract(String rawEmlContent) {
        try {
            Session session = Session.getInstance(new Properties());
            MimeMessage message = new MimeMessage(session,
                    new ByteArrayInputStream(rawEmlContent.getBytes(StandardCharsets.UTF_8)));

            Object content = firstTextContent(message);
            if (content == null) {
                throw new IllegalArgumentException("No text or html part found in email");
            }
            String text = htmlToText(content.toString());
            if (text.isBlank()) {
                throw new IllegalArgumentException("No readable text found in email");
            }
            return text;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse .eml content", e);
        }
    }

    private Object firstTextContent(jakarta.mail.Part part) throws Exception {
        if (part.isMimeType("text/html") || part.isMimeType("text/plain")) {
            return part.getContent();
        }
        if (part.isMimeType("multipart/*")) {
            jakarta.mail.Multipart multipart = (jakarta.mail.Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                Object content = firstTextContent(multipart.getBodyPart(i));
                if (content != null) {
                    return content;
                }
            }
        }
        return null;
    }

    private String htmlToText(String html) {
        Document document = Jsoup.parse(html);
        document.select("style, script, head").remove();
        document.outputSettings().prettyPrint(false);
        // Block elements to line breaks so the receipt's tabular structure survives
        document.select("br, p, div, tr, li, table, h1, h2, h3").append("\\n");
        String text = document.text().replace("\\n", "\n");
        return text.lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .reduce(new StringBuilder(), (sb, line) -> sb.append(line).append('\n'), StringBuilder::append)
                .toString();
    }
}
