import type { TextDocument, Range, Diagnostic } from "vscode"
import { CURSOR_MARKER } from "../ghostConstants"
import { DiagnosticSeverityNames } from "./diagnostics"

export function addCursorMarker(document: TextDocument, range?: Range): string {
	if (!range) return document.getText()

	const fullText = document.getText()
	const cursorOffset = document.offsetAt(range.start)
	const beforeCursor = fullText.substring(0, cursorOffset)
	const afterCursor = fullText.substring(cursorOffset)

	return `${beforeCursor}${CURSOR_MARKER}${afterCursor}`
}

export function formatDiagnostics(diagnostics: Diagnostic[]): string {
	if (!diagnostics || diagnostics.length === 0) return ""

	let result = "## Active Issues\n"

	const sorted = [...diagnostics].sort((a, b) => a.severity - b.severity)

	sorted.forEach((d) => {
		const severity = DiagnosticSeverityNames[d.severity] || "Unknown"
		const line = d.range.start.line + 1
		result += `- **${severity}** at line ${line}: ${d.message}\n`
	})

	return result
}

export function formatDocumentWithCursor(document: TextDocument, range?: Range, languageId?: string): string {
	const lang = languageId || document.languageId
	const codeWithCursor = addCursorMarker(document, range)

	return `\`\`\`${lang}
${codeWithCursor}
\`\`\``
}
