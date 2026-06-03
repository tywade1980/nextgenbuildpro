import { NextRequest, NextResponse } from 'next/server'
import { AIModelManager } from '@/lib/ai-models'

// Mock call data for demonstration
const mockCalls = [
  {
    id: '1',
    phoneNumber: '+1-555-0123',
    clientId: '1',
    status: 'answered',
    duration: 180,
    transcript: 'Client called regarding the residential complex project timeline. Discussed current progress and upcoming milestones.',
    summary: 'Project status update call - client satisfied with progress',
    sentiment: 'positive',
    priority: 'normal',
    createdAt: new Date().toISOString()
  },
  {
    id: '2',
    phoneNumber: '+1-555-9999',
    status: 'screened',
    duration: 30,
    transcript: 'Automated sales call detected. Caller was asking about general construction services.',
    summary: 'Sales call - not a priority',
    sentiment: 'neutral',
    priority: 'low',
    createdAt: new Date(Date.now() - 60000).toISOString()
  }
]

export async function GET() {
  try {
    return NextResponse.json({ 
      success: true, 
      calls: mockCalls,
      count: mockCalls.length 
    })
  } catch (error) {
    console.error('Error fetching calls:', error)
    return NextResponse.json(
      { success: false, error: 'Failed to fetch calls' },
      { status: 500 }
    )
  }
}

export async function POST(request: NextRequest) {
  try {
    const { phoneNumber, transcript, clientId } = await request.json()
    
    if (!phoneNumber || !transcript) {
      return NextResponse.json(
        { success: false, error: 'Phone number and transcript are required' },
        { status: 400 }
      )
    }

    // Use AI to analyze the call
    const aiManager = AIModelManager.getInstance()
    const analysis = await aiManager.analyzeCall(transcript)
    
    const newCall = {
      id: (mockCalls.length + 1).toString(),
      phoneNumber,
      clientId: clientId || null,
      status: 'answered' as const,
      duration: Math.floor(transcript.length / 5), // Rough estimate based on transcript length
      transcript,
      summary: analysis.summary,
      sentiment: analysis.sentiment,
      priority: analysis.priority,
      createdAt: new Date().toISOString()
    }
    
    mockCalls.unshift(newCall)
    
    return NextResponse.json({
      success: true,
      call: newCall,
      analysis
    })
  } catch (error) {
    console.error('Error processing call:', error)
    return NextResponse.json(
      { success: false, error: 'Failed to process call' },
      { status: 500 }
    )
  }
}