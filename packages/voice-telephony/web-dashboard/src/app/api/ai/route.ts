import { NextRequest, NextResponse } from 'next/server'
import { AIModelManager } from '@/lib/ai-models'

const aiManager = AIModelManager.getInstance()

export async function GET() {
  try {
    await aiManager.loadModels()
    const models = aiManager.getActiveModels()
    
    return NextResponse.json({ 
      success: true, 
      models,
      count: models.length 
    })
  } catch (error) {
    console.error('Error loading AI models:', error)
    return NextResponse.json(
      { success: false, error: 'Failed to load AI models' },
      { status: 500 }
    )
  }
}

export async function POST(request: NextRequest) {
  try {
    const { prompt, modelName, options } = await request.json()
    
    if (!prompt) {
      return NextResponse.json(
        { success: false, error: 'Prompt is required' },
        { status: 400 }
      )
    }

    const response = await aiManager.generateResponse(prompt, modelName, options)
    
    return NextResponse.json({
      success: true,
      response,
      model: modelName || 'GPT-4 Turbo'
    })
  } catch (error) {
    console.error('Error generating AI response:', error)
    return NextResponse.json(
      { success: false, error: 'Failed to generate response' },
      { status: 500 }
    )
  }
}