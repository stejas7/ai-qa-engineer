package com.aiqa.pipeline;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Turns raw business-requirement document text into one or more {@link RequirementBlock}s.
 *
 * <p>A single uploaded document commonly describes several distinct requirements (features,
 * user stories, epics). This splitter uses simple, dependency-free heuristics so the platform
 * never blocks on document structure:</p>
 * <ul>
 *   <li>Lines that look like headings ("Requirement 1", "REQ-002", "## Login", "1. Checkout flow")
 *       start a new requirement block.</li>
 *   <li>Bullet lines ("- ...", "* ...", "AC: ...") within a block become acceptance criteria.</li>
 *   <li>If no headings are found at all, the whole document becomes a single requirement.</li>
 * </ul>
 */
@Component
public class RequirementSplitter {

    private static final Pattern HEADING = Pattern.compile(
            "^(#{1,3}\\s*.+|(requirement|feature|user story|epic|req)[\\s#:.-]*\\d*\\s*[:.\\-]?\\s*.+|\\d+[.)]\\s+.+)$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern BULLET = Pattern.compile("^\\s*([-*•]|AC\\s*[:\\-])\\s*(.+)$", Pattern.CASE_INSENSITIVE);

    public List<RequirementBlock> split(String rawText, String fallbackTitle) {
        List<String> lines = rawText.lines().map(String::stripTrailing).toList();

        List<int[]> headingRanges = new ArrayList<>(); // [lineIndex]
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isBlank() && HEADING.matcher(line).matches() && line.length() < 140) {
                headingRanges.add(new int[]{i});
            }
        }

        List<RequirementBlock> blocks = new ArrayList<>();
        if (headingRanges.isEmpty()) {
            RequirementBlock block = buildBlock(fallbackTitle, lines, 0, lines.size());
            if (!block.description().isBlank()) blocks.add(block);
            return blocks;
        }

        for (int h = 0; h < headingRanges.size(); h++) {
            int startLine = headingRanges.get(h)[0];
            int endLine = (h + 1 < headingRanges.size()) ? headingRanges.get(h + 1)[0] : lines.size();
            String title = cleanHeading(lines.get(startLine));
            RequirementBlock block = buildBlock(title, lines, startLine + 1, endLine);
            if (!block.description().isBlank() || !block.acceptanceCriteria().isEmpty()) {
                blocks.add(block);
            }
        }

        if (blocks.isEmpty()) {
            RequirementBlock block = buildBlock(fallbackTitle, lines, 0, lines.size());
            blocks.add(block);
        }
        return blocks;
    }

    private RequirementBlock buildBlock(String title, List<String> lines, int from, int to) {
        StringBuilder description = new StringBuilder();
        List<String> criteria = new ArrayList<>();
        for (int i = from; i < to; i++) {
            String line = lines.get(i);
            if (line.isBlank()) continue;
            var bulletMatch = BULLET.matcher(line.trim());
            if (bulletMatch.matches()) {
                criteria.add(bulletMatch.group(2).trim());
            } else {
                description.append(line.trim()).append(" ");
            }
        }
        String desc = description.toString().trim();
        return new RequirementBlock(
                title == null || title.isBlank() ? "Untitled requirement" : title,
                desc.isBlank() ? title : desc,
                criteria);
    }

    private String cleanHeading(String heading) {
        return heading.trim()
                .replaceFirst("^#{1,3}\\s*", "")
                .replaceFirst("(?i)^(requirement|feature|user story|epic|req)[\\s#:.-]*\\d*\\s*[:.\\-]?\\s*", "")
                .replaceFirst("^\\d+[.)]\\s*", "")
                .trim();
    }

    public record RequirementBlock(String title, String description, List<String> acceptanceCriteria) {}
}
