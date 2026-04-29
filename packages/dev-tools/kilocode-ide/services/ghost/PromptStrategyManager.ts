import { GhostSuggestionContext } from "./types"
import { PromptStrategy } from "./types/PromptStrategy"

// Import all strategies
import { UserRequestStrategy } from "./strategies/UserRequestStrategy"
import { SelectionRefactorStrategy } from "./strategies/SelectionRefactorStrategy"
import { CommentDrivenStrategy } from "./strategies/CommentDrivenStrategy"
import { NewLineCompletionStrategy } from "./strategies/NewLineCompletionStrategy"
import { InlineCompletionStrategy } from "./strategies/InlineCompletionStrategy"
import { AutoTriggerStrategy } from "./strategies/AutoTriggerStrategy"

/**
 * Manages prompt strategies and selects the appropriate one based on context
 */
export class PromptStrategyManager {
	private strategies: PromptStrategy[]
	private autoTriggerStrategy: AutoTriggerStrategy
	private debug: boolean

	constructor(options?: { debug: boolean }) {
		this.debug = options?.debug ?? false

		// Register all strategies in priority order
		this.strategies = [
			new UserRequestStrategy(),
			new SelectionRefactorStrategy(),
			new NewLineCompletionStrategy(),
			new CommentDrivenStrategy(),
			new InlineCompletionStrategy(),
		]
		this.autoTriggerStrategy = new AutoTriggerStrategy()
	}

	/**
	 * Selects the most appropriate strategy for the given context
	 * @param context The suggestion context
	 * @returns The selected strategy
	 */
	selectStrategy(context: GhostSuggestionContext): PromptStrategy {
		const strategy = this.strategies.find((s) => s.canHandle(context)) ?? this.autoTriggerStrategy

		if (this.debug) {
			console.log(`[PromptStrategyManager] Selected strategy: ${strategy.name}`)
		}

		return strategy
	}

	/**
	 * Builds complete prompts using the selected strategy
	 * @param context The suggestion context
	 * @returns Object containing system and user prompts
	 */
	buildPrompt(context: GhostSuggestionContext): {
		systemPrompt: string
		userPrompt: string
		strategy: PromptStrategy
	} {
		const strategy = this.selectStrategy(context)

		const systemPrompt = strategy.getSystemInstructions()
		const userPrompt = strategy.getUserPrompt(context)

		if (this.debug) {
			console.log("[PromptStrategyManager] Prompt built:", {
				strategy: strategy.name,
				systemPromptLength: systemPrompt.length,
				userPromptLength: userPrompt.length,
				totalTokensEstimate: Math.ceil((systemPrompt.length + userPrompt.length) / 4),
			})
		}

		return {
			systemPrompt,
			userPrompt,
			strategy,
		}
	}
}
