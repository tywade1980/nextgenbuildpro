import { EventEmitter } from "events"
import fs from "fs/promises"
import * as path from "path"
import * as os from "os"

import * as vscode from "vscode"

import {
	type RooCodeAPI,
	type RooCodeSettings,
	type RooCodeEvents,
	type ProviderSettings,
	type ProviderSettingsEntry,
	type TaskEvent,
	type CreateTaskOptions,
	RooCodeEventName,
	TaskCommandName,
	isSecretStateKey,
	IpcOrigin,
	IpcMessageType,
} from "@roo-code/types"
import { IpcServer } from "@roo-code/ipc"

import { Package } from "../shared/package"
import { ClineProvider } from "../core/webview/ClineProvider"
import { openClineInNewTab } from "../activate/registerCommands"

export class API extends EventEmitter<RooCodeEvents> implements RooCodeAPI {
	private readonly outputChannel: vscode.OutputChannel
	private readonly sidebarProvider: ClineProvider
	private readonly context: vscode.ExtensionContext
	private readonly ipc?: IpcServer
	private readonly taskMap = new Map<string, ClineProvider>()
	private readonly log: (...args: unknown[]) => void
	private logfile?: string

	constructor(
		outputChannel: vscode.OutputChannel,
		provider: ClineProvider,
		socketPath?: string,
		enableLogging = false,
	) {
		super()

		this.outputChannel = outputChannel
		this.sidebarProvider = provider
		this.context = provider.context

		if (enableLogging) {
			this.log = (...args: unknown[]) => {
				this.outputChannelLog(...args)
				console.log(args)
			}

			this.logfile = path.join(os.tmpdir(), "kilo-code-messages.log")
		} else {
			this.log = () => {}
		}

		this.registerListeners(this.sidebarProvider)

		if (socketPath) {
			const ipc = (this.ipc = new IpcServer(socketPath, this.log))

			ipc.listen()
			this.log(`[API] ipc server started: socketPath=${socketPath}, pid=${process.pid}, ppid=${process.ppid}`)

			ipc.on(IpcMessageType.TaskCommand, async (_clientId, { commandName, data }) => {
				switch (commandName) {
					case TaskCommandName.StartNewTask:
						this.log(`[API] StartNewTask -> ${data.text}, ${JSON.stringify(data.configuration)}`)
						await this.startNewTask(data)
						break
					case TaskCommandName.CancelTask:
						this.log(`[API] CancelTask -> ${data}`)
						await this.cancelTask(data)
						break
					case TaskCommandName.CloseTask:
						this.log(`[API] CloseTask -> ${data}`)
						await vscode.commands.executeCommand("workbench.action.files.saveFiles")
						await vscode.commands.executeCommand("workbench.action.closeWindow")
						break
					case TaskCommandName.ResumeTask:
						this.log(`[API] ResumeTask -> ${data}`)
						try {
							await this.resumeTask(data)
						} catch (error) {
							const errorMessage = error instanceof Error ? error.message : String(error)
							this.log(`[API] ResumeTask failed for taskId ${data}: ${errorMessage}`)
							// Don't rethrow - we want to prevent IPC server crashes
							// The error is logged for debugging purposes
						}
						break
				}
			})
		}
	}

	public override emit<K extends keyof RooCodeEvents>(
		eventName: K,
		...args: K extends keyof RooCodeEvents ? RooCodeEvents[K] : never
	) {
		const data = { eventName: eventName as RooCodeEventName, payload: args } as TaskEvent
		this.ipc?.broadcast({ type: IpcMessageType.TaskEvent, origin: IpcOrigin.Server, data })
		return super.emit(eventName, ...args)
	}

	public async startNewTask({
		configuration,
		text,
		images,
		newTab,
	}: {
		configuration: RooCodeSettings
		text?: string
		images?: string[]
		newTab?: boolean
	}) {
		let provider: ClineProvider

		if (newTab) {
			await vscode.commands.executeCommand("workbench.action.files.revert")
			await vscode.commands.executeCommand("workbench.action.closeAllEditors")

			provider = await openClineInNewTab({ context: this.context, outputChannel: this.outputChannel })
			this.registerListeners(provider)
		} else {
			await vscode.commands.executeCommand(`${Package.name}.SidebarProvider.focus`)

			provider = this.sidebarProvider
		}

		await provider.removeClineFromStack()
		await provider.postStateToWebview()
		await provider.postMessageToWebview({ type: "action", action: "chatButtonClicked" })
		await provider.postMessageToWebview({ type: "invoke", invoke: "newChat", text, images })

		const options: CreateTaskOptions = {
			consecutiveMistakeLimit: Number.MAX_SAFE_INTEGER,
		}

		const task = await provider.createTask(text, images, undefined, options, configuration)

		if (!task) {
			throw new Error("Failed to create task due to policy restrictions")
		}

		return task.taskId
	}

	public async resumeTask(taskId: string): Promise<void> {
		const { historyItem } = await this.sidebarProvider.getTaskWithId(taskId)
		await this.sidebarProvider.createTaskWithHistoryItem(historyItem)
		await this.sidebarProvider.postMessageToWebview({ type: "action", action: "chatButtonClicked" })
	}

	public async isTaskInHistory(taskId: string): Promise<boolean> {
		try {
			await this.sidebarProvider.getTaskWithId(taskId)
			return true
		} catch {
			return false
		}
	}

	public getCurrentTaskStack() {
		return this.sidebarProvider.getCurrentTaskStack()
	}

	public async clearCurrentTask(lastMessage?: string) {
		await this.sidebarProvider.finishSubTask(lastMessage ?? "")
		await this.sidebarProvider.postStateToWebview()
	}

	public async cancelCurrentTask() {
		await this.sidebarProvider.cancelTask()
	}

	public async cancelTask(taskId: string) {
		const provider = this.taskMap.get(taskId)

		if (provider) {
			await provider.cancelTask()
			this.taskMap.delete(taskId)
		}
	}

	public async sendMessage(text?: string, images?: string[]) {
		await this.sidebarProvider.postMessageToWebview({ type: "invoke", invoke: "sendMessage", text, images })
	}

	public async pressPrimaryButton() {
		await this.sidebarProvider.postMessageToWebview({ type: "invoke", invoke: "primaryButtonClick" })
	}

	public async pressSecondaryButton() {
		await this.sidebarProvider.postMessageToWebview({ type: "invoke", invoke: "secondaryButtonClick" })
	}

	public isReady() {
		return this.sidebarProvider.viewLaunched
	}

	// kilocode_change start
	public async buildApp(
		appType: string,
		appName: string,
		targetPath?: string,
		options?: Record<string, any>,
	): Promise<string> {
		// Generate a task description for building the app
		const taskDescription = this.generateAppBuildTask(appType, appName, targetPath, options)

		// Start a new task with the generated description
		return await this.startNewTask({
			configuration: this.getConfiguration(),
			text: taskDescription,
		})
	}

	private generateAppBuildTask(
		appType: string,
		appName: string,
		targetPath?: string,
		options?: Record<string, any>,
	): string {
		const path = targetPath || `./${appName}`
		let task = `Create a new ${appType} application named "${appName}" in the directory "${path}".\n\n`

		// Add app type specific instructions
		switch (appType.toLowerCase()) {
			case "react":
				task += `Please create a modern React application with the following structure:
- Use Vite or Create React App
- Include TypeScript support
- Set up a basic component structure
- Add a README with setup instructions
- Include essential dependencies`
				break
			case "nodejs":
			case "node":
				task += `Please create a Node.js application with the following structure:
- Initialize npm project with package.json
- Set up a basic Express server (if web application)
- Include TypeScript configuration
- Add a README with setup instructions
- Set up basic project structure`
				break
			case "python":
				task += `Please create a Python application with the following structure:
- Set up virtual environment configuration
- Create requirements.txt
- Set up basic project structure
- Add a README with setup instructions
- Include main application file`
				break
			case "nextjs":
			case "next":
				task += `Please create a Next.js application with the following structure:
- Use create-next-app
- Include TypeScript support
- Set up app router
- Add a README with setup instructions
- Include essential dependencies`
				break
			case "android":
				task += `Please create a native Android application with the following structure:
- Use Kotlin as the primary programming language
- Set up Android project with Gradle build system
- Include Android Jetpack components (ViewModel, LiveData, etc.)
- Set up Material Design 3 components
- Add a README with setup instructions
- Configure for both debug and release builds`
				break
			default:
				task += `Please create a ${appType} application following best practices and conventions for this type of project.`
		}

		// Add any custom options
		if (options && Object.keys(options).length > 0) {
			task += `\n\nAdditional requirements:\n`
			for (const [key, value] of Object.entries(options)) {
				if (key === "enableDevMode" && value === true) {
					task += `- Enable Dev Mode: Set up the application with hot-reload/live-reload capabilities, embedded development tools, and a command-line interface for real-time iteration during runtime. This should include:\n`
					task += `  * Configure the app to run in development mode with debugging enabled\n`
					task += `  * Set up hot module replacement or live reload\n`
					task += `  * Include a development server or runtime environment\n`
					task += `  * Add terminal/console access for runtime commands\n`
					task += `  * Configure for easy iteration and testing on device/emulator\n`
				} else {
					task += `- ${key}: ${value}\n`
				}
			}
		}

		return task
	}
	// kilocode_change end

	private registerListeners(provider: ClineProvider) {
		provider.on(RooCodeEventName.TaskCreated, (task) => {
			// Task Lifecycle

			task.on(RooCodeEventName.TaskStarted, async () => {
				this.emit(RooCodeEventName.TaskStarted, task.taskId)
				this.taskMap.set(task.taskId, provider)
				await this.fileLog(`[${new Date().toISOString()}] taskStarted -> ${task.taskId}\n`)
			})

			task.on(RooCodeEventName.TaskCompleted, async (_, tokenUsage, toolUsage) => {
				this.emit(RooCodeEventName.TaskCompleted, task.taskId, tokenUsage, toolUsage, {
					isSubtask: !!task.parentTaskId,
				})

				this.taskMap.delete(task.taskId)

				await this.fileLog(
					`[${new Date().toISOString()}] taskCompleted -> ${task.taskId} | ${JSON.stringify(tokenUsage, null, 2)} | ${JSON.stringify(toolUsage, null, 2)}\n`,
				)
			})

			task.on(RooCodeEventName.TaskAborted, () => {
				this.emit(RooCodeEventName.TaskAborted, task.taskId)
				this.taskMap.delete(task.taskId)
			})

			task.on(RooCodeEventName.TaskFocused, () => {
				this.emit(RooCodeEventName.TaskFocused, task.taskId)
			})

			task.on(RooCodeEventName.TaskUnfocused, () => {
				this.emit(RooCodeEventName.TaskUnfocused, task.taskId)
			})

			task.on(RooCodeEventName.TaskActive, () => {
				this.emit(RooCodeEventName.TaskActive, task.taskId)
			})

			task.on(RooCodeEventName.TaskInteractive, () => {
				this.emit(RooCodeEventName.TaskInteractive, task.taskId)
			})

			task.on(RooCodeEventName.TaskResumable, () => {
				this.emit(RooCodeEventName.TaskResumable, task.taskId)
			})

			task.on(RooCodeEventName.TaskIdle, () => {
				this.emit(RooCodeEventName.TaskIdle, task.taskId)
			})

			// Subtask Lifecycle

			task.on(RooCodeEventName.TaskPaused, () => {
				this.emit(RooCodeEventName.TaskPaused, task.taskId)
			})

			task.on(RooCodeEventName.TaskUnpaused, () => {
				this.emit(RooCodeEventName.TaskUnpaused, task.taskId)
			})

			task.on(RooCodeEventName.TaskSpawned, (childTaskId) => {
				this.emit(RooCodeEventName.TaskSpawned, task.taskId, childTaskId)
			})

			// Task Execution

			task.on(RooCodeEventName.Message, async (message) => {
				this.emit(RooCodeEventName.Message, { taskId: task.taskId, ...message })

				if (message.message.partial !== true) {
					await this.fileLog(`[${new Date().toISOString()}] ${JSON.stringify(message.message, null, 2)}\n`)
				}
			})

			task.on(RooCodeEventName.TaskModeSwitched, (taskId, mode) => {
				this.emit(RooCodeEventName.TaskModeSwitched, taskId, mode)
			})

			task.on(RooCodeEventName.TaskAskResponded, () => {
				this.emit(RooCodeEventName.TaskAskResponded, task.taskId)
			})

			// Task Analytics

			task.on(RooCodeEventName.TaskToolFailed, (taskId, tool, error) => {
				this.emit(RooCodeEventName.TaskToolFailed, taskId, tool, error)
			})

			task.on(RooCodeEventName.TaskTokenUsageUpdated, (_, usage) => {
				this.emit(RooCodeEventName.TaskTokenUsageUpdated, task.taskId, usage)
			})

			// Let's go!

			this.emit(RooCodeEventName.TaskCreated, task.taskId)
		})
	}

	// Logging

	private outputChannelLog(...args: unknown[]) {
		for (const arg of args) {
			if (arg === null) {
				this.outputChannel.appendLine("null")
			} else if (arg === undefined) {
				this.outputChannel.appendLine("undefined")
			} else if (typeof arg === "string") {
				this.outputChannel.appendLine(arg)
			} else if (arg instanceof Error) {
				this.outputChannel.appendLine(`Error: ${arg.message}\n${arg.stack || ""}`)
			} else {
				try {
					this.outputChannel.appendLine(
						JSON.stringify(
							arg,
							(key, value) => {
								if (typeof value === "bigint") return `BigInt(${value})`
								if (typeof value === "function") return `Function: ${value.name || "anonymous"}`
								if (typeof value === "symbol") return value.toString()
								return value
							},
							2,
						),
					)
				} catch (error) {
					this.outputChannel.appendLine(`[Non-serializable object: ${Object.prototype.toString.call(arg)}]`)
				}
			}
		}
	}

	private async fileLog(message: string) {
		if (!this.logfile) {
			return
		}

		try {
			await fs.appendFile(this.logfile, message, "utf8")
		} catch (_) {
			this.logfile = undefined
		}
	}

	// Global Settings Management

	public getConfiguration(): RooCodeSettings {
		return Object.fromEntries(
			Object.entries(this.sidebarProvider.getValues()).filter(([key]) => !isSecretStateKey(key)),
		)
	}

	public async setConfiguration(values: RooCodeSettings) {
		await this.sidebarProvider.contextProxy.setValues(values)
		await this.sidebarProvider.providerSettingsManager.saveConfig(values.currentApiConfigName || "default", values)
		await this.sidebarProvider.postStateToWebview()
	}

	// Provider Profile Management

	public getProfiles(): string[] {
		return this.sidebarProvider.getProviderProfileEntries().map(({ name }) => name)
	}

	public getProfileEntry(name: string): ProviderSettingsEntry | undefined {
		return this.sidebarProvider.getProviderProfileEntry(name)
	}

	public async createProfile(name: string, profile?: ProviderSettings, activate: boolean = true) {
		const entry = this.getProfileEntry(name)

		if (entry) {
			throw new Error(`Profile with name "${name}" already exists`)
		}

		const id = await this.sidebarProvider.upsertProviderProfile(name, profile ?? {}, activate)

		if (!id) {
			throw new Error(`Failed to create profile with name "${name}"`)
		}

		return id
	}

	public async updateProfile(
		name: string,
		profile: ProviderSettings,
		activate: boolean = true,
	): Promise<string | undefined> {
		const entry = this.getProfileEntry(name)

		if (!entry) {
			throw new Error(`Profile with name "${name}" does not exist`)
		}

		const id = await this.sidebarProvider.upsertProviderProfile(name, profile, activate)

		if (!id) {
			throw new Error(`Failed to update profile with name "${name}"`)
		}

		return id
	}

	public async upsertProfile(
		name: string,
		profile: ProviderSettings,
		activate: boolean = true,
	): Promise<string | undefined> {
		const id = await this.sidebarProvider.upsertProviderProfile(name, profile, activate)

		if (!id) {
			throw new Error(`Failed to upsert profile with name "${name}"`)
		}

		return id
	}

	public async deleteProfile(name: string): Promise<void> {
		const entry = this.getProfileEntry(name)

		if (!entry) {
			throw new Error(`Profile with name "${name}" does not exist`)
		}

		await this.sidebarProvider.deleteProviderProfile(entry)
	}

	public getActiveProfile(): string | undefined {
		return this.getConfiguration().currentApiConfigName
	}

	public async setActiveProfile(name: string): Promise<string | undefined> {
		const entry = this.getProfileEntry(name)

		if (!entry) {
			throw new Error(`Profile with name "${name}" does not exist`)
		}

		await this.sidebarProvider.activateProviderProfile({ name })
		return this.getActiveProfile()
	}
}
