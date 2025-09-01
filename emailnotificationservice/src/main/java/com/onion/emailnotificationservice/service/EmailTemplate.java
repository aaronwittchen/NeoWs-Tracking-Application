package com.onion.emailnotificationservice.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmailTemplate {
    
    public static String createEmailHtml(String asteroidContent, String apodContent, LocalDateTime generatedTime, String userName) {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>");
        htmlContent.append("<html lang=\"en\">");
        htmlContent.append("<head>");
        htmlContent.append("<meta charset=\"UTF-8\">");
        htmlContent.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        htmlContent.append("<title>NASA Space Watch Weekly</title>");
        htmlContent.append("<style>");
        
        // More natural, less perfect styling
        htmlContent.append("body { font-family: Georgia, 'Times New Roman', serif; margin: 0; padding: 20px; background: #fafaf9; color: #2c2c2c; line-height: 1.6; }");
        htmlContent.append(".container { max-width: 650px; margin: 0 auto; background: #ffffff; border: 1px solid #ddd; }");
        htmlContent.append(".header { background: #2b4c85; color: #fff; padding: 25px 30px; border-bottom: 3px solid #1e3a5f; }");
        htmlContent.append(".header h1 { margin: 0 0 8px 0; font-size: 28px; font-weight: normal; }");
        htmlContent.append(".header .subtitle { margin: 0; font-size: 14px; color: #b8c5d1; font-style: italic; }");
        htmlContent.append(".content { padding: 30px; }");
        htmlContent.append(".intro { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px 20px; margin-bottom: 25px; font-size: 15px; }");
        htmlContent.append(".section-header { font-size: 20px; color: #2b4c85; margin: 30px 0 15px 0; border-bottom: 2px solid #e9ecef; padding-bottom: 5px; }");
        htmlContent.append(".asteroid-item { background: #f8f9fa; border: 1px solid #dee2e6; margin-bottom: 20px; padding: 20px; }");
        htmlContent.append(".asteroid-name { font-size: 18px; font-weight: bold; color: #495057; margin-bottom: 12px; }");
        htmlContent.append(".details-grid { display: table; width: 100%; }");
        htmlContent.append(".detail-row { display: table-row; }");
        htmlContent.append(".detail-label, .detail-value { display: table-cell; padding: 6px 0; vertical-align: top; }");
        htmlContent.append(".detail-label { font-weight: bold; width: 35%; color: #666; padding-right: 15px; }");
        htmlContent.append(".detail-value { color: #333; }");
        htmlContent.append(".space-photo { border: 1px solid #ccc; margin: 25px 0; padding: 15px; background: #fdfdfd; }");
        htmlContent.append(".photo-title { font-size: 18px; color: #2b4c85; margin-bottom: 15px; text-align: center; }");
        htmlContent.append(".photo-img { width: 100%; height: auto; border: 1px solid #ddd; margin-bottom: 15px; }");
        htmlContent.append(".photo-desc { font-size: 14px; color: #555; text-align: justify; margin-bottom: 10px; }");
        htmlContent.append(".photo-meta { font-size: 12px; color: #888; text-align: center; border-top: 1px solid #eee; padding-top: 10px; }");
        htmlContent.append(".footer { background: #f1f3f4; padding: 20px 30px; font-size: 12px; color: #666; border-top: 1px solid #ddd; }");
        htmlContent.append(".footer p { margin: 5px 0; }");
        htmlContent.append(".risk-high { color: #d73527; font-weight: bold; }");
        htmlContent.append(".risk-medium { color: #f57c00; font-weight: bold; }");
        htmlContent.append(".risk-low { color: #2e7d32; font-weight: bold; }");
        htmlContent.append("@media (max-width: 600px) {");
        htmlContent.append("  body { padding: 10px; }");
        htmlContent.append("  .container { border: none; }");
        htmlContent.append("  .content { padding: 20px 15px; }");
        htmlContent.append("  .details-grid { display: block; }");
        htmlContent.append("  .detail-row { display: block; margin-bottom: 8px; }");
        htmlContent.append("  .detail-label, .detail-value { display: block; width: 100%; padding: 2px 0; }");
        htmlContent.append("}");
        htmlContent.append("</style>");
        htmlContent.append("</head>");
        htmlContent.append("<body>");
        htmlContent.append("<div class=\"container\">");
        htmlContent.append("<div class=\"header\">");
        htmlContent.append("<h1>NASA Space Watch</h1>");
        htmlContent.append("<div class=\"subtitle\">Weekly Near-Earth Object Report</div>");
        htmlContent.append("</div>");
        htmlContent.append("<div class=\"content\">");
        htmlContent.append("<div class=\"intro\">");
        htmlContent.append("Hello ").append(userName != null ? userName : "Space Enthusiast")
        htmlContent.append("! This week we're tracking several interesting objects making close approaches to Earth. ");
        htmlContent.append("While none pose any immediate threat, they provide valuable opportunities for scientific observation.");
        htmlContent.append("</div>");
        
        if (asteroidContent != null && !asteroidContent.isEmpty()) {
            htmlContent.append("<div class=\"section-header\">This Week's Close Approaches</div>");
            htmlContent.append(asteroidContent);
        }
        
        if (apodContent != null && !apodContent.isEmpty()) {
            htmlContent.append(apodContent);
        }
        
        htmlContent.append("</div>");
        htmlContent.append("<div class=\"footer\">");
        htmlContent.append("<p><strong>NASA's Center for Near Earth Object Studies (CNEOS)</strong></p>");
        htmlContent.append("<p>Report generated on ").append(formatDateTime(generatedTime)).append("</p>");
        htmlContent.append("<p>Data provided by JPL's Small-Body Database and NASA's Astronomy Picture of the Day</p>");
        htmlContent.append("</div>");
        htmlContent.append("</div>");
        htmlContent.append("</body>");
        htmlContent.append("</html>");
        
        return htmlContent.toString();
    }
    
    public static String createAsteroidCardHtml(String asteroidName, String closeApproachDate, 
                                              double estimatedDiameter, double missDistance, String riskLevel) {
        StringBuilder card = new StringBuilder();
        card.append("<div class=\"asteroid-item\">");
        card.append("<div class=\"asteroid-name\">").append(cleanAsteroidName(asteroidName)).append("</div>");
        card.append("<div class=\"details-grid\">");
        
        card.append("<div class=\"detail-row\">");
        card.append("<div class=\"detail-label\">Closest Approach:</div>");
        card.append("<div class=\"detail-value\">").append(escapeHtml(closeApproachDate)).append("</div>");
        card.append("</div>");
        
        card.append("<div class=\"detail-row\">");
        card.append("<div class=\"detail-label\">Estimated Size:</div>");
        card.append("<div class=\"detail-value\">").append(formatDiameter(estimatedDiameter)).append("</div>");
        card.append("</div>");
        
        card.append("<div class=\"detail-row\">");
        card.append("<div class=\"detail-label\">Distance at Closest:</div>");
        card.append("<div class=\"detail-value\">").append(formatDistance(missDistance)).append("</div>");
        card.append("</div>");
        
        card.append("<div class=\"detail-row\">");
        card.append("<div class=\"detail-label\">Assessment:</div>");
        card.append("<div class=\"detail-value\">").append(riskLevel).append("</div>");
        card.append("</div>");
        
        card.append("</div>");
        card.append("</div>");
        return card.toString();
    }
    
    public static String createApodSectionHtml(String title, String imageUrl, String explanation, 
                                              String date, String copyright) {
        StringBuilder apod = new StringBuilder();
        apod.append("<div class=\"space-photo\">");
        apod.append("<div class=\"photo-title\">Featured Space Image</div>");
        apod.append("<img src=\"").append(escapeHtml(imageUrl)).append("\" alt=\"").append(escapeHtml(title)).append("\" class=\"photo-img\">");
        apod.append("<div class=\"photo-desc\">");
        apod.append("<strong>").append(escapeHtml(title)).append("</strong><br><br>");
        apod.append(escapeHtml(explanation));
        apod.append("</div>");
        apod.append("<div class=\"photo-meta\">");
        apod.append("Image Date: ").append(escapeHtml(date));
        if (copyright != null && !copyright.isEmpty()) {
            apod.append(" • Credit: ").append(escapeHtml(copyright));
        }
        apod.append("</div>");
        apod.append("</div>");
        return apod.toString();
    }
    
    public static String getRiskLevelHtml(double missDistance) {
        if (missDistance < 1000000) {
            return "<span class=\"risk-high\">High Interest</span>";
        } else if (missDistance < 5000000) {
            return "<span class=\"risk-medium\">Moderate Interest</span>";
        } else {
            return "<span class=\"risk-low\">Routine Observation</span>";
        }
    }
    
    private static String cleanAsteroidName(String name) {
        if (name == null) return "Unknown Object";
        
        // Remove common prefixes and make more readable
        String cleaned = name.replaceAll("^\\([0-9]+\\)\\s*", "")
                            .replaceAll("^[0-9]+\\s+", "")
                            .trim();
        
        if (cleaned.isEmpty()) {
            return name; // fallback to original
        }
        
        return escapeHtml(cleaned);
    }
    
    private static String formatDiameter(double diameter) {
        if (diameter > 1000) {
            return String.format("%.1f km", diameter / 1000);
        } else {
            return String.format("%.0f meters", diameter);
        }
    }
    
    private static String formatDistance(double distance) {
        double lunarDistances = distance / 384400; // km to lunar distances
        if (lunarDistances < 1) {
            return String.format("%.0f km (%.2f lunar distances)", distance, lunarDistances);
        } else {
            return String.format("%.0f km (%.1f lunar distances)", distance, lunarDistances);
        }
    }
    
    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a"));
    }
    
    private static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}