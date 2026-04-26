import type { TextDocument, Range, Diagnostic } from "vscode"
import { GhostSuggestionContext } from "../types"
import { PromptStrategy, UseCaseType } from "../types/PromptStrategy"

/**
 * Abstract base class for all prompt strategies
 * Provides common functionality and enforces consistent structure
 */
export abstract class BasePromptStrategy implements PromptStrategy {
	/**
	 * Human-readable name of the strategy
	 */
	abstract name: string

	/**
	 * The use case type this strategy handles
	 */
	abstract type: UseCaseType

	/**
	 * Determines if this strategy can handle the given context
	 */
	abstract canHandle(context: GhostSuggestionContext): boolean

	/**
	 * Gets the base system instructions that apply to all strategies
	 */
	protected getBaseSystemInstructions(): string {
		return `CRITICAL OUTPUT FORMAT:
You must respond with XML-formatted changes ONLY. No explanations or text outside XML tags.

Format: <change><search><![CDATA[exact_code]]></search><replace><![CDATA[new_code]]></replace></change>

MANDATORY XML STRUCTURE RULES:
- Every <change> tag MUST have a closing </change> tag
- Every <search> tag MUST have a closing </search> tag
- Every <replace> tag MUST have a closing </replace> tag
- Every <![CDATA[ MUST have a closing ]]>
- XML tags should be properly formatted and nested
- Multiple <change> blocks allowed for different modifications

CHANGE ORDERING PRIORITY:
- CRITICAL: Order all <change> blocks by proximity to the cursor marker (<<<AUTOCOMPLETE_HERE>>>)
- Put changes closest to the cursor marker FIRST in your response
- This allows immediate display of the most relevant suggestions to the user
- Changes further from the cursor should come later in the response
- Measure proximity by line distance from the cursor marker position

CONTENT MATCHING RULES:
- Search content must match EXACTLY (including whitespace, indentation, and line breaks)
- Use CDATA wrappers for all code content
- Preserve all line breaks and formatting within CDATA sections
- Never generate overlapping changes
- The <search> block must contain exact text that exists in the code
- If you can't find exact match, don't generate that change

EXAMPLE:
<change><search><![CDATA[function example() {
	 // old code
}]]></search><replace><![CDATA[function example() {
	 // new code
}]]></replace></change>

--

`
	}

	/**
	 * Gets strategy-specific system instructions
	 * Must be implemented by each strategy
	 */
	abstract getSystemInstructions(): string

	/**
	 * Generates the user prompt with context
	 */
	abstract getUserPrompt(context: GhostSuggestionContext): string
}
