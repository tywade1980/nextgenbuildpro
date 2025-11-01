# Meeting Recording & Actionable Item Extraction

This feature enables NextGen BuildPro to record phone calls and in-person meetings, automatically transcribe them, and extract actionable items using AI.

## Features

### 1. Meeting Recording
- **In-Person Meetings**: Manually start/stop recording with optional pause/resume
- **Phone Calls**: Automatically record calls when enabled
- **Video Calls**: Support for recording video conference audio

### 2. Automatic Transcription
- Uses OpenAI's Whisper API for high-quality speech-to-text transcription
- Construction industry terminology optimized
- Handles various audio qualities and accents

### 3. AI-Powered Actionable Item Extraction
- Extracts tasks, commitments, and follow-up items from transcriptions
- Assigns priorities (LOW, MEDIUM, HIGH, CRITICAL) automatically
- Detects due dates and deadlines mentioned in conversations
- Intelligently routes tasks to appropriate departmental orchestrators based on content
- Identifies project references and dependencies

## Architecture

### Core Components

1. **MeetingRecordingService** (`crm/service/MeetingRecordingService.kt`)
   - Handles audio recording for different meeting types
   - Manages recording lifecycle (start, pause, resume, stop)
   - Supports automatic call recording when enabled

2. **VoiceRecorderService** (`crm/service/VoiceRecorderService.kt`)
   - Provides audio transcription using OpenAI Whisper API
   - Handles secure API key storage and retrieval

3. **ActionableItemExtractor** (`crm/service/ActionableItemExtractor.kt`)
   - Uses LLM to intelligently parse transcriptions
   - Extracts actionable items with context
   - Determines appropriate agent assignments based on task content

4. **MeetingRecordingManager** (`crm/service/MeetingRecordingManager.kt`)
   - Orchestrates the complete flow: record → transcribe → extract → create tasks
   - Manages processing state and recent meetings
   - Integrates with MainOrchestrator for task routing

5. **MeetingRecordingScreen** (`features/recording/MeetingRecordingScreen.kt`)
   - UI for controlling meeting recordings
   - Displays processing status and recent meetings
   - Shows extracted actionable items

### Data Models (`crm/service/MeetingRecordingModels.kt`)

- `MeetingRecording`: Recording metadata and transcription
- `MeetingType`: PHONE_CALL, IN_PERSON, VIDEO_CALL
- `RecordingState`: IDLE, RECORDING, PAUSED, PROCESSING
- `ProcessedMeeting`: Complete meeting data with extracted tasks
- `MeetingContext`: Context for AI extraction

## Task Routing Intelligence

The system automatically routes extracted tasks to appropriate orchestrators:

- **Financial tasks** → CFO Financial Orchestrator (estimates, budgets, costs, invoices)
- **Design tasks** → CTO Design Orchestrator (blueprints, CAD, specifications)
- **Safety tasks** → CSO Safety Orchestrator (permits, inspections, compliance, OSHA)
- **Client tasks** → CHRO Client/HR Orchestrator (client meetings, contracts, follow-ups)
- **Operations tasks** → COO Operations Orchestrator (scheduling, equipment, materials, crews)
- **General tasks** → CEO Personal Assistant (default for unclassified tasks)

## Usage

### Recording an In-Person Meeting

```kotlin
val meetingRecordingManager = MeetingRecordingManager(context, llmService, mainOrchestrator)

// Start recording
meetingRecordingManager.startMeetingRecording(
    meetingType = MeetingType.IN_PERSON,
    meetingTitle = "Project Kickoff Meeting",
    participants = listOf("John Smith", "Jane Doe")
)

// Stop and process
val result = meetingRecordingManager.stopAndProcessMeeting()
if (result.isSuccess) {
    val processedMeeting = result.getOrNull()
    // processedMeeting contains transcription and extracted tasks
}
```

### Auto-Recording Phone Calls

```kotlin
// Enable auto-recording
meetingRecordingManager.setAutoRecordCalls(true)

// Calls will now be automatically recorded and processed
// No manual intervention needed
```

### Using the UI

The `MeetingRecordingScreen` provides a complete UI for:
- Starting/stopping manual recordings
- Toggling auto-record for calls
- Viewing processing status (transcribing, extracting, creating tasks)
- Browsing recent meetings and their extracted items

## Permissions Required

The following permissions are already declared in `AndroidManifest.xml`:

- `RECORD_AUDIO`: For recording audio
- `READ_PHONE_STATE`: For detecting call states
- `READ_CALL_LOG`/`WRITE_CALL_LOG`: For call recording integration
- `FOREGROUND_SERVICE`: For background recording

## Configuration

### API Keys

The system uses the OpenAI API for transcription. Configure your API key in the app settings:

```kotlin
SecureCredentialStorage.KEY_SPEECH_API
```

### LLM Service

The ActionableItemExtractor uses the configured LLM service (typically OpenRouter) for intelligent extraction. Ensure your LLM service is properly initialized in the application context.

## File Storage

Recordings are stored in the app's external files directory:
- Path: `{EXTERNAL_FILES_DIR}/Music/meeting_recordings/`
- Format: 3GP (AMR-NB encoding)
- Naming: `meeting_YYYYMMDD_HHmmss.3gp`

## Integration Points

### With MainOrchestrator
Extracted tasks are routed through `MainOrchestrator.delegateTask()` which distributes them to appropriate departmental orchestrators.

### With LLM Service
Uses the configured LLM service for AI-powered extraction with construction-specific system prompts.

### With CEOPersonalAssistantOrchestrator
The personal assistant can trigger recordings and review extracted items on behalf of the user.

## Testing

### Unit Tests
Test files should be created in:
- `app/src/test/java/com/nextgenbuildpro/crm/service/`
- `app/src/androidTest/java/com/nextgenbuildpro/features/recording/`

### Manual Testing
1. Start the app and navigate to Meeting Recording screen
2. Grant microphone permissions when prompted
3. Start an in-person meeting recording
4. Speak some actionable items (e.g., "John needs to submit the permit application by Friday")
5. Stop recording and observe transcription and extraction
6. Verify tasks appear in the appropriate department's task list

## Future Enhancements

- Support for video recording
- Multi-language transcription
- Speaker diarization (identify who said what)
- Meeting summaries and key points extraction
- Integration with calendar for automatic meeting recording
- Real-time transcription during meetings
- Sentiment analysis for client interactions
- Automatic meeting minutes generation

## Security Considerations

- API keys are stored securely using `SecureCredentialStorage`
- Recordings are stored locally and not automatically uploaded
- Transcriptions may be sent to third-party APIs (OpenAI) - ensure compliance with privacy policies
- Consider implementing user consent workflows for call recording
- Comply with local laws regarding call recording (some jurisdictions require two-party consent)

## Performance

- Transcription time depends on audio length and API response time
- LLM extraction typically adds 2-5 seconds per meeting
- Recordings use compressed 3GP format to minimize storage
- Processing happens asynchronously to avoid blocking UI

## Troubleshooting

### "Transcription failed" error
- Check that OpenAI API key is configured correctly
- Verify internet connection
- Ensure audio file exists and is not corrupted

### "Failed to start recording" error
- Check microphone permission is granted
- Ensure no other app is using the microphone
- Verify sufficient storage space

### No actionable items extracted
- Check LLM service is properly configured
- Verify the transcription contains clear action items
- Review system prompts in `ActionableItemExtractor`

## Dependencies

- Android MediaRecorder API for audio recording
- OpenAI Whisper API for transcription
- Configured LLM Service for extraction
- Kotlin Coroutines for async operations
- Jetpack Compose for UI
