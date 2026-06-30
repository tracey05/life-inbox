package com.lifeinbox.evaluation;

import com.lifeinbox.domain.LifeItemCandidate;
import com.lifeinbox.domain.LifeItemType;
import com.lifeinbox.llm.HeuristicLifeItemExtractor;
import com.lifeinbox.llm.LifeItemExtractor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class EvaluationRunner {
    public static void main(String[] args) {
        LifeItemExtractor extractor = new HeuristicLifeItemExtractor(
                Clock.fixed(Instant.parse("2026-06-30T00:00:00Z"), ZoneId.of("Asia/Shanghai")));

        int pass = 0;
        List<EvaluationCase> cases = seedCases();
        for (EvaluationCase evaluationCase : cases) {
            EvaluationResult result = new EvaluationResult(evaluationCase, extractor.extract(evaluationCase.getInput()).getItems());
            if (result.typeSequenceMatches()) {
                pass++;
            }
            printResult(result);
        }

        System.out.println();
        System.out.println("Type sequence matches: " + pass + "/" + cases.size());
    }

    private static void printResult(EvaluationResult result) {
        System.out.println("[" + result.getEvaluationCase().getId() + "] " +
                (result.typeSequenceMatches() ? "PASS" : "REVIEW"));
        System.out.println("Input: " + result.getEvaluationCase().getInput());
        System.out.println("Expected: " + result.getEvaluationCase().getExpectedTypes());
        System.out.println("Actual:   " + result.actualTypes());
        for (LifeItemCandidate candidate : result.getCandidates()) {
            System.out.println("  - " + candidate.getType() + " | " + candidate.getTitle() +
                    " | missing=" + candidate.getMissingInformation() +
                    " | evidence=" + candidate.getEvidence());
        }
    }

    public static List<EvaluationCase> seedCases() {
        List<EvaluationCase> cases = new ArrayList<EvaluationCase>();
        cases.add(new EvaluationCase("single-appointment",
                "Your dentist appointment is scheduled for July 12 at 10:30 AM.",
                LifeItemType.EVENT));
        cases.add(new EvaluationCase("event-and-deadline",
                "The workshop is on August 3. Registration closes on July 25.",
                LifeItemType.EVENT, LifeItemType.DEADLINE));
        cases.add(new EvaluationCase("missing-year",
                "Your membership renews automatically on September 10.",
                LifeItemType.EVENT));
        cases.add(new EvaluationCase("fuzzy-task",
                "We should probably renew the insurance sometime next month.",
                LifeItemType.TASK));
        cases.add(new EvaluationCase("reference-only",
                "The air purifier model is AP-300, and the filter type is F-12.",
                LifeItemType.REFERENCE));
        cases.add(new EvaluationCase("no-useful-info",
                "This product looks quite good. I may consider it later."));
        cases.add(new EvaluationCase("multi-mixed",
                "The flight leaves on July 18 at 8:30 AM. Online check-in opens 24 hours earlier, and baggage must be purchased before July 16.",
                LifeItemType.EVENT, LifeItemType.EVENT, LifeItemType.DEADLINE));
        cases.add(new EvaluationCase("cancelled-history",
                "The previous appointment was on June 20, but it has now been cancelled."));
        cases.add(new EvaluationCase("hotel-two-items",
                "Your hotel reservation is confirmed for July 18-21. Free cancellation is available until July 15.",
                LifeItemType.EVENT, LifeItemType.DEADLINE));
        cases.add(new EvaluationCase("explicit-year-event",
                "Your dentist appointment is scheduled for July 12, 2026 at 10:30 AM.",
                LifeItemType.EVENT));
        cases.add(new EvaluationCase("explicit-year-deadline",
                "Registration closes on July 25, 2026.",
                LifeItemType.DEADLINE));
        cases.add(new EvaluationCase("iso-date-flight",
                "The flight leaves on 2026-07-18 at 8:30 AM.",
                LifeItemType.EVENT));
        cases.add(new EvaluationCase("reference-address",
                "The repair center address is 15 River Road, Suite 2.",
                LifeItemType.REFERENCE));
        cases.add(new EvaluationCase("task-call-support",
                "Need to contact customer support about the refund.",
                LifeItemType.TASK));
        cases.add(new EvaluationCase("negated-appointment",
                "I do not have a dentist appointment on July 12."));
        cases.add(new EvaluationCase("past-meeting",
                "The meeting happened on June 1."));
        cases.add(new EvaluationCase("bill-deadline",
                "The electricity bill must be paid before August 9.",
                LifeItemType.DEADLINE));
        cases.add(new EvaluationCase("course-event",
                "The course starts on September 2.",
                LifeItemType.EVENT));
        cases.add(new EvaluationCase("warranty-reference",
                "The warranty lasts 24 months from purchase.",
                LifeItemType.REFERENCE));
        cases.add(new EvaluationCase("application-deadline",
                "The application deadline is October 1.",
                LifeItemType.DEADLINE));
        cases.add(new EvaluationCase("upload-task",
                "Please upload your proof of address.",
                LifeItemType.TASK));
        cases.add(new EvaluationCase("delivery-event",
                "Delivery is scheduled for November 8 between 2 PM and 5 PM.",
                LifeItemType.EVENT));
        cases.add(new EvaluationCase("renewal-warning",
                "Your plan renews on December 3 unless cancelled.",
                LifeItemType.EVENT));
        cases.add(new EvaluationCase("travel-mixed",
                "Hotel check-in is on July 18. Free cancellation ends July 15.",
                LifeItemType.EVENT, LifeItemType.DEADLINE));
        cases.add(new EvaluationCase("receipt-reference",
                "Order number is A12345 and the serial number is SN-9921.",
                LifeItemType.REFERENCE));
        cases.add(new EvaluationCase("ambiguous-suggestion",
                "Maybe we can visit the museum sometime in August."));
        cases.add(new EvaluationCase("maintenance-event",
                "Maintenance is scheduled for August 6, 2026 at 9 AM.",
                LifeItemType.EVENT));
        cases.add(new EvaluationCase("before-wording",
                "Submit the reimbursement documents before September 5.",
                LifeItemType.DEADLINE));
        cases.add(new EvaluationCase("class-cancelled",
                "The class on July 7 has been cancelled."));
        cases.add(new EvaluationCase("useful-but-no-action",
                "The door code is 4821.",
                LifeItemType.REFERENCE));
        return cases;
    }
}
