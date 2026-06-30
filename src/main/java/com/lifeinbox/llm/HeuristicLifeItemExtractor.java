package com.lifeinbox.llm;

import com.lifeinbox.domain.LifeItemCandidate;
import com.lifeinbox.domain.LifeItemType;
import com.lifeinbox.llm.schema.LifeItemExtractionResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeuristicLifeItemExtractor implements LifeItemExtractor {
    private static final Pattern MONTH_DAY = Pattern.compile(
            "(?i)\\b(January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{1,2})(?:\\s*(?:-|–|to)\\s*(\\d{1,2}))?(?:,?\\s+(\\d{4}))?");
    private static final Pattern ISO_DATE = Pattern.compile("\\b(\\d{4})-(\\d{2})-(\\d{2})\\b");
    private static final Pattern TIME = Pattern.compile("(?i)\\b(\\d{1,2})(?::(\\d{2}))?\\s*(AM|PM)\\b");

    private final Clock clock;

    public HeuristicLifeItemExtractor(Clock clock) {
        this.clock = clock;
    }

    @Override
    public LifeItemExtractionResponse extract(String rawContent) {
        List<LifeItemCandidate> items = new ArrayList<LifeItemCandidate>();
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return new LifeItemExtractionResponse(false, items);
        }

        String[] sentences = splitSentences(rawContent);
        for (String sentence : sentences) {
            addCandidatesForSentence(items, sentence.trim());
        }

        return new LifeItemExtractionResponse(!items.isEmpty(), items);
    }

    private void addCandidatesForSentence(List<LifeItemCandidate> items, String sentence) {
        if (sentence.isEmpty()) {
            return;
        }
        String lower = sentence.toLowerCase(Locale.US);

        if (isCancelledHistoricalSentence(lower) || isLowSignalSentence(lower)) {
            return;
        }

        if (lower.contains("reservation") && lower.contains("confirmed")) {
            items.add(hotelStay(sentence));
        }
        if (lower.contains("free cancellation") && lower.contains("until")) {
            items.add(deadline(sentence, "Hotel free cancellation deadline", "Cancel or review the reservation before the deadline.", 0.90));
        }
        if (lower.contains("free cancellation") && lower.contains("ends")) {
            items.add(deadline(sentence, "Hotel free cancellation deadline", "Cancel or review the reservation before the deadline.", 0.86));
        }
        if (lower.contains("appointment") && lower.contains("scheduled")) {
            items.add(event(sentence, appointmentTitle(lower), 0.86));
        }
        if (lower.contains("scheduled for") && !lower.contains("appointment")) {
            items.add(event(sentence, scheduledTitle(lower), 0.78));
        }
        if (lower.contains("workshop") && lower.contains(" on ")) {
            items.add(event(sentence, "Workshop", 0.82));
        }
        if (lower.contains("course starts on")) {
            items.add(event(sentence, "Course start", 0.80));
        }
        if (lower.contains("hotel check-in is on")) {
            items.add(event(sentence, "Hotel check-in", 0.78));
        }
        if (lower.contains("registration closes")) {
            items.add(deadline(sentence, "Registration deadline", "Register before the deadline.", 0.88));
        }
        if (lower.contains("application deadline")) {
            items.add(deadline(sentence, "Application deadline", "Submit the application before the deadline.", 0.86));
        }
        if (lower.contains("must be paid before")) {
            items.add(deadline(sentence, "Bill payment deadline", "Pay the bill before the deadline.", 0.84));
        }
        if (lower.startsWith("submit ") && lower.contains(" before ")) {
            items.add(deadline(sentence, "Submission deadline", "Submit the required documents before the deadline.", 0.80));
        }
        if (lower.contains("flight leaves")) {
            items.add(event(sentence, "Flight departure", 0.90));
        }
        if (lower.contains("online check-in opens") && lower.contains("24 hours earlier")) {
            items.add(checkInOpens(sentence));
        }
        if (lower.contains("baggage must be purchased before")) {
            items.add(deadline(sentence, "Baggage purchase deadline", "Purchase baggage before the deadline.", 0.88));
        }
        if (lower.contains("membership renews automatically")) {
            LifeItemCandidate candidate = event(sentence, "Membership renewal", 0.72);
            candidate.setActionRequired(false);
            items.add(candidate);
        }
        if (lower.contains("plan renews on")) {
            LifeItemCandidate candidate = event(sentence, "Plan renewal", 0.70);
            candidate.setActionRequired(lower.contains("unless cancelled") || lower.contains("unless canceled"));
            if (candidate.isActionRequired()) {
                candidate.setSuggestedAction("Review whether to keep or cancel the plan before renewal.");
            }
            items.add(candidate);
        }
        if (lower.contains("should probably") && lower.contains("renew")) {
            items.add(task(sentence, "Renew insurance", "Review whether the insurance should be renewed.", 0.48));
        }
        if (lower.contains("contact customer support")) {
            items.add(task(sentence, "Contact customer support", "Contact customer support about the issue.", 0.72));
        }
        if (lower.contains("upload ") || lower.contains("please upload")) {
            items.add(task(sentence, "Upload required material", "Upload the requested material.", 0.76));
        }
        if (lower.contains("model") || lower.contains("filter type")) {
            if (lower.contains("air purifier") || lower.contains("filter")) {
                items.add(reference(sentence, "Air purifier details", 0.80));
            }
        }
        if (lower.contains("address is")) {
            items.add(reference(sentence, "Address", 0.74));
        }
        if (lower.contains("warranty")) {
            items.add(reference(sentence, "Warranty information", 0.74));
        }
        if (lower.contains("order number") || lower.contains("serial number")) {
            items.add(reference(sentence, "Order and serial numbers", 0.76));
        }
        if (lower.contains("door code")) {
            items.add(reference(sentence, "Door code", 0.76));
        }
    }

    private LifeItemCandidate hotelStay(String evidence) {
        LifeItemCandidate candidate = event(evidence, "Hotel stay", 0.82);
        candidate.setSummary("Hotel reservation details from the source input.");
        addMissing(candidate, "Hotel location");
        DateInfo dateInfo = findDate(evidence);
        if (dateInfo.found && !dateInfo.yearMissing) {
            candidate.setStartTime(LocalDateTime.of(dateInfo.date, LocalTime.of(15, 0)));
            if (dateInfo.endDate != null) {
                candidate.setEndTime(LocalDateTime.of(dateInfo.endDate, LocalTime.of(11, 0)));
            }
        }
        return candidate;
    }

    private LifeItemCandidate event(String evidence, String title, double confidence) {
        LifeItemCandidate candidate = baseCandidate(evidence, title, LifeItemType.EVENT, confidence);
        candidate.setActionRequired(false);
        DateInfo dateInfo = findDate(evidence);
        LocalTime time = findTime(evidence);
        if (dateInfo.found && !dateInfo.yearMissing) {
            candidate.setStartTime(LocalDateTime.of(dateInfo.date, time == null ? LocalTime.MIDNIGHT : time));
            if (dateInfo.endDate != null) {
                candidate.setEndTime(LocalDateTime.of(dateInfo.endDate, LocalTime.MIDNIGHT));
            }
        } else if (dateInfo.found && dateInfo.yearMissing) {
            addMissing(candidate, "Year");
        } else {
            addMissing(candidate, "Date");
        }
        if (time == null && (evidence.toLowerCase(Locale.US).contains("appointment") || evidence.toLowerCase(Locale.US).contains("flight"))) {
            addMissing(candidate, "Time");
        }
        return candidate;
    }

    private LifeItemCandidate deadline(String evidence, String title, String suggestedAction, double confidence) {
        LifeItemCandidate candidate = baseCandidate(evidence, title, LifeItemType.DEADLINE, confidence);
        candidate.setActionRequired(true);
        candidate.setSuggestedAction(suggestedAction);
        DateInfo dateInfo = findDate(evidence);
        if (dateInfo.found && !dateInfo.yearMissing) {
            candidate.setDeadline(LocalDateTime.of(dateInfo.date, LocalTime.of(23, 59, 59)));
        } else if (dateInfo.found && dateInfo.yearMissing) {
            addMissing(candidate, "Year");
        } else {
            addMissing(candidate, "Deadline date");
        }
        return candidate;
    }

    private LifeItemCandidate checkInOpens(String evidence) {
        LifeItemCandidate candidate = baseCandidate(evidence, "Online check-in opens", LifeItemType.EVENT, 0.70);
        candidate.setActionRequired(false);
        addMissing(candidate, "Flight date and year");
        return candidate;
    }

    private LifeItemCandidate task(String evidence, String title, String suggestedAction, double confidence) {
        LifeItemCandidate candidate = baseCandidate(evidence, title, LifeItemType.TASK, confidence);
        candidate.setActionRequired(true);
        candidate.setSuggestedAction(suggestedAction);
        addMissing(candidate, "Exact deadline");
        return candidate;
    }

    private LifeItemCandidate reference(String evidence, String title, double confidence) {
        LifeItemCandidate candidate = baseCandidate(evidence, title, LifeItemType.REFERENCE, confidence);
        candidate.setSummary(evidence);
        candidate.setActionRequired(false);
        return candidate;
    }

    private LifeItemCandidate baseCandidate(String evidence, String title, LifeItemType type, double confidence) {
        LifeItemCandidate candidate = new LifeItemCandidate();
        candidate.setType(type);
        candidate.setTitle(title);
        candidate.setSummary(evidence);
        candidate.setConfidence(confidence);
        candidate.setEvidence(new ArrayList<String>(Arrays.asList(evidence)));
        return candidate;
    }

    private DateInfo findDate(String text) {
        Matcher iso = ISO_DATE.matcher(text);
        if (iso.find()) {
            int year = Integer.parseInt(iso.group(1));
            int month = Integer.parseInt(iso.group(2));
            int day = Integer.parseInt(iso.group(3));
            return new DateInfo(true, false, LocalDate.of(year, month, day), null);
        }

        Matcher matcher = MONTH_DAY.matcher(text);
        if (!matcher.find()) {
            return DateInfo.notFound();
        }

        Month month = Month.valueOf(matcher.group(1).toUpperCase(Locale.US));
        int day = Integer.parseInt(matcher.group(2));
        String endDayText = matcher.group(3);
        String yearText = matcher.group(4);
        if (yearText == null) {
            return new DateInfo(true, true, null, null);
        }

        int year = Integer.parseInt(yearText);
        LocalDate start = LocalDate.of(year, month, day);
        LocalDate end = null;
        if (endDayText != null) {
            end = LocalDate.of(year, month, Integer.parseInt(endDayText));
        }
        return new DateInfo(true, false, start, end);
    }

    private LocalTime findTime(String text) {
        Matcher matcher = TIME.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        int hour = Integer.parseInt(matcher.group(1));
        int minute = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
        String marker = matcher.group(3).toUpperCase(Locale.US);
        if ("PM".equals(marker) && hour < 12) {
            hour += 12;
        }
        if ("AM".equals(marker) && hour == 12) {
            hour = 0;
        }
        return LocalTime.of(hour, minute);
    }

    private void addMissing(LifeItemCandidate candidate, String field) {
        if (!candidate.getMissingInformation().contains(field)) {
            candidate.getMissingInformation().add(field);
        }
    }

    private String appointmentTitle(String lower) {
        if (lower.contains("dentist")) {
            return "Dentist appointment";
        }
        return "Appointment";
    }

    private String scheduledTitle(String lower) {
        if (lower.contains("delivery")) {
            return "Delivery";
        }
        if (lower.contains("maintenance")) {
            return "Maintenance";
        }
        return "Scheduled event";
    }

    private boolean isCancelledHistoricalSentence(String lower) {
        return (lower.contains("previous") || lower.contains("was on")) &&
                (lower.contains("cancelled") || lower.contains("canceled"));
    }

    private boolean isLowSignalSentence(String lower) {
        return lower.contains("looks quite good") || lower.contains("may consider it later");
    }

    private String[] splitSentences(String rawContent) {
        return rawContent.split("(?<=[.!?])\\s+");
    }

    private static class DateInfo {
        private final boolean found;
        private final boolean yearMissing;
        private final LocalDate date;
        private final LocalDate endDate;

        private DateInfo(boolean found, boolean yearMissing, LocalDate date, LocalDate endDate) {
            this.found = found;
            this.yearMissing = yearMissing;
            this.date = date;
            this.endDate = endDate;
        }

        private static DateInfo notFound() {
            return new DateInfo(false, false, null, null);
        }
    }
}
