import { NextRequest, NextResponse } from 'next/server';
import { AIModelManager } from '@/lib/ai-models';

export async function GET() {
  try {
    const modelManager = AIModelManager.getInstance();
    const models = await modelManager.getModels();
    
    return NextResponse.json({
      success: true,
      data: models
    });
  } catch (error) {
    console.error('Error fetching AI models:', error);
    return NextResponse.json({
      success: false,
      error: 'Failed to fetch AI models'
    }, { status: 500 });
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { action, modelId, modelData } = body;
    
    const modelManager = AIModelManager.getInstance();
    
    switch (action) {
      case 'load':
        const success = await modelManager.loadModel(modelId);
        return NextResponse.json({
          success,
          message: success ? 'Model loaded successfully' : 'Failed to load model'
        });
        
      case 'add':
        const newModelId = await modelManager.addModel(modelData);
        return NextResponse.json({
          success: true,
          data: { id: newModelId },
          message: 'Model added successfully'
        });
        
      case 'execute':
        const { prompt, options } = body;
        const result = await modelManager.executeRequest(modelId, prompt, options);
        return NextResponse.json({
          success: true,
          data: { result },
          message: 'Request executed successfully'
        });
        
      default:
        return NextResponse.json({
          success: false,
          error: 'Invalid action'
        }, { status: 400 });
    }
  } catch (error) {
    console.error('Error in AI models API:', error);
    return NextResponse.json({
      success: false,
      error: 'Internal server error'
    }, { status: 500 });
  }
}