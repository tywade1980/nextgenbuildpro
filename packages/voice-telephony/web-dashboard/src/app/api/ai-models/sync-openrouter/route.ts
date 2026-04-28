import { NextResponse } from 'next/server';
import { AIModelManager } from '@/lib/ai-models';
import { openRouterService } from '@/lib/openrouter';

export async function POST() {
  try {
    if (!openRouterService.isConfigured()) {
      return NextResponse.json({
        success: false,
        error: 'OpenRouter API key not configured. Please set OPENROUTER_API_KEY environment variable.'
      }, { status: 400 });
    }

    const modelManager = AIModelManager.getInstance();
    const addedCount = await modelManager.syncOpenRouterModels();
    
    return NextResponse.json({
      success: true,
      data: { addedCount },
      message: `Successfully synced ${addedCount} new OpenRouter models`
    });
  } catch (error) {
    console.error('Error syncing OpenRouter models:', error);
    return NextResponse.json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to sync OpenRouter models'
    }, { status: 500 });
  }
}

export async function GET() {
  try {
    if (!openRouterService.isConfigured()) {
      return NextResponse.json({
        success: false,
        error: 'OpenRouter API key not configured'
      }, { status: 400 });
    }

    const modelManager = AIModelManager.getInstance();
    const openRouterModels = await modelManager.getOpenRouterModels();
    
    return NextResponse.json({
      success: true,
      data: openRouterModels,
      count: openRouterModels.length
    });
  } catch (error) {
    console.error('Error fetching OpenRouter models:', error);
    return NextResponse.json({
      success: false,
      error: error instanceof Error ? error.message : 'Failed to fetch OpenRouter models'
    }, { status: 500 });
  }
}