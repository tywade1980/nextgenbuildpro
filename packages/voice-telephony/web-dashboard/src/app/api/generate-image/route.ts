import { NextRequest, NextResponse } from 'next/server';
import { ConstructionImageGenerator, ImageGenerationOptions } from '@/lib/imageGeneration';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const options: ImageGenerationOptions = {
      topic: body.topic,
      style: body.style,
      lighting: body.lighting,
      perspective: body.perspective,
      customPrompt: body.customPrompt,
      size: body.size || '1024x1024',
      quality: body.quality || 'standard'
    };

    const generator = new ConstructionImageGenerator();
    const result = await generator.generateImage(options);

    if (result.error) {
      return NextResponse.json(
        { error: result.error },
        { status: 500 }
      );
    }

    return NextResponse.json({
      url: result.url,
      prompt: result.prompt,
      success: true
    });

  } catch (error) {
    console.error('API Error:', error);
    return NextResponse.json(
      { error: 'Failed to generate image' },
      { status: 500 }
    );
  }
}

export async function GET() {
  const generator = new ConstructionImageGenerator();
  const options = generator.getAvailableOptions();
  
  return NextResponse.json(options);
}