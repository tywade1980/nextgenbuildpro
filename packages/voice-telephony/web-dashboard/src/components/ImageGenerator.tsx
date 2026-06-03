'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { ImageGenerationOptions, ImageModificationOptions, ImageModifier } from '@/lib/imageGeneration';
import { RefreshCw, Download, Settings, Wand2, Image as ImageIcon, Info } from 'lucide-react';
import VarietyDemo from './VarietyDemo';

interface GeneratedImage {
  url: string;
  prompt: string;
  id: string;
  timestamp: number;
}

interface AvailableOptions {
  topics: string[];
  styles: string[];
  lighting: string[];
  perspectives: string[];
}

export default function ImageGenerator() {
  const [generatedImages, setGeneratedImages] = useState<GeneratedImage[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);
  const [currentImage, setCurrentImage] = useState<GeneratedImage | null>(null);
  const [availableOptions, setAvailableOptions] = useState<AvailableOptions | null>(null);
  const [imageOptions, setImageOptions] = useState<ImageGenerationOptions>({
    size: '1024x1024',
    quality: 'standard'
  });
  const [modificationOptions, setModificationOptions] = useState<ImageModificationOptions>({
    brightness: 0,
    contrast: 0,
    saturation: 0,
    sepia: false,
    grayscale: false,
    blur: 0
  });
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [showVarietyDemo, setShowVarietyDemo] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchAvailableOptions();
  }, []);

  const fetchAvailableOptions = async () => {
    try {
      const response = await fetch('/api/generate-image');
      const options = await response.json();
      setAvailableOptions(options);
    } catch (err) {
      console.error('Failed to fetch options:', err);
    }
  };

  const generateImage = async () => {
    setIsGenerating(true);
    setError(null);

    try {
      const response = await fetch('/api/generate-image', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(imageOptions),
      });

      const result = await response.json();

      if (result.error) {
        setError(result.error);
        return;
      }

      const newImage: GeneratedImage = {
        url: result.url,
        prompt: result.prompt,
        id: Date.now().toString(),
        timestamp: Date.now()
      };

      setGeneratedImages(prev => [newImage, ...prev.slice(0, 9)]); // Keep last 10 images
      setCurrentImage(newImage);

    } catch (err) {
      setError('Failed to generate image. Please try again.');
      console.error('Generation error:', err);
    } finally {
      setIsGenerating(false);
    }
  };

  const generateRandomImage = async () => {
    setImageOptions({
      ...imageOptions,
      topic: undefined,
      style: undefined,
      lighting: undefined,
      perspective: undefined,
      customPrompt: undefined
    });
    await generateImage();
  };

  const downloadImage = async (imageUrl: string, filename: string) => {
    try {
      const response = await fetch(imageUrl);
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      console.error('Download failed:', err);
    }
  };

  const applyImageFilters = (imageUrl: string): string => {
    return ImageModifier.applyFilters(imageUrl, modificationOptions);
  };

  const resetFilters = () => {
    setModificationOptions({
      brightness: 0,
      contrast: 0,
      saturation: 0,
      sepia: false,
      grayscale: false,
      blur: 0
    });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 to-slate-800 text-white p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold mb-4 bg-gradient-to-r from-blue-400 to-cyan-300 bg-clip-text text-transparent">
            Construction AI Image Generator
          </h1>
          <p className="text-xl text-slate-300 max-w-2xl mx-auto">
            Generate unlimited construction-themed images with AI. Endless variety of styles, perspectives, and ideas for the construction industry.
          </p>
        </div>

        {/* Controls */}
        <div className="mb-8 bg-slate-800/50 rounded-xl p-6 backdrop-blur-sm border border-slate-700">
          <div className="flex flex-wrap gap-4 mb-6">
            <button
              onClick={generateRandomImage}
              disabled={isGenerating}
              className="flex items-center gap-2 bg-gradient-to-r from-blue-600 to-cyan-600 hover:from-blue-700 hover:to-cyan-700 disabled:opacity-50 disabled:cursor-not-allowed px-6 py-3 rounded-lg font-medium transition-all duration-200 shadow-lg hover:shadow-xl"
            >
              <Wand2 className="w-5 h-5" />
              {isGenerating ? 'Generating...' : 'Generate Random'}
            </button>

            <button
              onClick={() => setShowVarietyDemo(true)}
              className="flex items-center gap-2 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 px-6 py-3 rounded-lg font-medium transition-all duration-200 shadow-lg hover:shadow-xl"
            >
              <Info className="w-5 h-5" />
              View Variety Options
            </button>

            <button
              onClick={() => setShowAdvanced(!showAdvanced)}
              className="flex items-center gap-2 bg-slate-700 hover:bg-slate-600 px-6 py-3 rounded-lg font-medium transition-all duration-200"
            >
              <Settings className="w-5 h-5" />
              Advanced Options
            </button>
          </div>

          {/* Advanced Options */}
          {showAdvanced && availableOptions && (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6 p-4 bg-slate-900/50 rounded-lg">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Topic</label>
                <select
                  value={imageOptions.topic || ''}
                  onChange={(e) => setImageOptions(prev => ({ ...prev, topic: e.target.value || undefined }))}
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Random Topic</option>
                  {availableOptions.topics.map(topic => (
                    <option key={topic} value={topic}>{topic}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Style</label>
                <select
                  value={imageOptions.style || ''}
                  onChange={(e) => setImageOptions(prev => ({ ...prev, style: e.target.value || undefined }))}
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Random Style</option>
                  {availableOptions.styles.map(style => (
                    <option key={style} value={style}>{style}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Lighting</label>
                <select
                  value={imageOptions.lighting || ''}
                  onChange={(e) => setImageOptions(prev => ({ ...prev, lighting: e.target.value || undefined }))}
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Random Lighting</option>
                  {availableOptions.lighting.map(lighting => (
                    <option key={lighting} value={lighting}>{lighting}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Perspective</label>
                <select
                  value={imageOptions.perspective || ''}
                  onChange={(e) => setImageOptions(prev => ({ ...prev, perspective: e.target.value || undefined }))}
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Random Perspective</option>
                  {availableOptions.perspectives.map(perspective => (
                    <option key={perspective} value={perspective}>{perspective}</option>
                  ))}
                </select>
              </div>

              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-slate-300 mb-2">Custom Prompt</label>
                <input
                  type="text"
                  value={imageOptions.customPrompt || ''}
                  onChange={(e) => setImageOptions(prev => ({ ...prev, customPrompt: e.target.value || undefined }))}
                  placeholder="Enter custom construction scene description..."
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Size</label>
                <select
                  value={imageOptions.size}
                  onChange={(e) => setImageOptions(prev => ({ ...prev, size: e.target.value as '1024x1024' | '1792x1024' | '1024x1792' }))}
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="1024x1024">Square (1024x1024)</option>
                  <option value="1792x1024">Landscape (1792x1024)</option>
                  <option value="1024x1792">Portrait (1024x1792)</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Quality</label>
                <select
                  value={imageOptions.quality}
                  onChange={(e) => setImageOptions(prev => ({ ...prev, quality: e.target.value as 'standard' | 'hd' }))}
                  className="w-full bg-slate-700 border border-slate-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="standard">Standard</option>
                  <option value="hd">HD Quality</option>
                </select>
              </div>
            </div>
          )}

          <button
            onClick={generateImage}
            disabled={isGenerating}
            className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 disabled:opacity-50 disabled:cursor-not-allowed px-6 py-3 rounded-lg font-medium transition-all duration-200 shadow-lg hover:shadow-xl"
          >
            {isGenerating ? (
              <>
                <RefreshCw className="w-5 h-5 animate-spin" />
                Generating Construction Image...
              </>
            ) : (
              <>
                <ImageIcon className="w-5 h-5" />
                Generate with Current Settings
              </>
            )}
          </button>
        </div>

        {/* Error Display */}
        {error && (
          <div className="mb-6 p-4 bg-red-900/50 border border-red-700 rounded-lg text-red-200">
            <strong>Error:</strong> {error}
          </div>
        )}

        {/* Current Image Display */}
        {currentImage && (
          <div className="mb-8 bg-slate-800/50 rounded-xl p-6 backdrop-blur-sm border border-slate-700">
            <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
              {/* Image Display */}
              <div className="lg:col-span-3">
                <div className="relative group">
                  <Image
                    src={currentImage.url}
                    alt="Generated construction scene"
                    width={1024}
                    height={1024}
                    className="w-full h-auto rounded-lg shadow-2xl transition-all duration-300"
                    style={{
                      filter: applyImageFilters(currentImage.url)
                    }}
                  />
                  <div className="absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                    <button
                      onClick={() => downloadImage(currentImage.url, `construction-${currentImage.id}.png`)}
                      className="bg-black/50 hover:bg-black/70 p-2 rounded-lg backdrop-blur-sm"
                    >
                      <Download className="w-5 h-5" />
                    </button>
                  </div>
                </div>
                <p className="mt-4 text-sm text-slate-400 italic">
                  <strong>Prompt:</strong> {currentImage.prompt}
                </p>
              </div>

              {/* Image Modification Controls */}
              <div className="space-y-4">
                <h3 className="text-lg font-semibold text-slate-200 mb-4">Image Modifications</h3>
                
                <div>
                  <label className="block text-sm text-slate-300 mb-2">Brightness: {modificationOptions.brightness}</label>
                  <input
                    type="range"
                    min="-100"
                    max="100"
                    value={modificationOptions.brightness}
                    onChange={(e) => setModificationOptions(prev => ({ ...prev, brightness: Number(e.target.value) }))}
                    className="w-full"
                  />
                </div>

                <div>
                  <label className="block text-sm text-slate-300 mb-2">Contrast: {modificationOptions.contrast}</label>
                  <input
                    type="range"
                    min="-100"
                    max="100"
                    value={modificationOptions.contrast}
                    onChange={(e) => setModificationOptions(prev => ({ ...prev, contrast: Number(e.target.value) }))}
                    className="w-full"
                  />
                </div>

                <div>
                  <label className="block text-sm text-slate-300 mb-2">Saturation: {modificationOptions.saturation}</label>
                  <input
                    type="range"
                    min="-100"
                    max="100"
                    value={modificationOptions.saturation}
                    onChange={(e) => setModificationOptions(prev => ({ ...prev, saturation: Number(e.target.value) }))}
                    className="w-full"
                  />
                </div>

                <div>
                  <label className="block text-sm text-slate-300 mb-2">Blur: {modificationOptions.blur}px</label>
                  <input
                    type="range"
                    min="0"
                    max="10"
                    value={modificationOptions.blur}
                    onChange={(e) => setModificationOptions(prev => ({ ...prev, blur: Number(e.target.value) }))}
                    className="w-full"
                  />
                </div>

                <div className="space-y-2">
                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={modificationOptions.sepia}
                      onChange={(e) => setModificationOptions(prev => ({ ...prev, sepia: e.target.checked }))}
                      className="rounded"
                    />
                    <span className="text-sm text-slate-300">Sepia Effect</span>
                  </label>

                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={modificationOptions.grayscale}
                      onChange={(e) => setModificationOptions(prev => ({ ...prev, grayscale: e.target.checked }))}
                      className="rounded"
                    />
                    <span className="text-sm text-slate-300">Grayscale</span>
                  </label>
                </div>

                <button
                  onClick={resetFilters}
                  className="w-full bg-slate-600 hover:bg-slate-500 px-4 py-2 rounded-lg text-sm font-medium transition-colors duration-200"
                >
                  Reset Filters
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Image Gallery */}
        {generatedImages.length > 0 && (
          <div className="bg-slate-800/50 rounded-xl p-6 backdrop-blur-sm border border-slate-700">
            <h2 className="text-2xl font-bold mb-6 text-slate-200">Recent Generations</h2>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
              {generatedImages.map((image) => (
                <div
                  key={image.id}
                  className={`relative group cursor-pointer rounded-lg overflow-hidden transition-all duration-200 hover:scale-105 ${
                    currentImage?.id === image.id ? 'ring-2 ring-blue-500' : ''
                  }`}
                  onClick={() => setCurrentImage(image)}
                >
                  <Image
                    src={image.url}
                    alt="Generated construction scene"
                    width={200}
                    height={128}
                    className="w-full h-32 object-cover"
                  />
                  <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex items-center justify-center">
                    <span className="text-white text-sm font-medium">View</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Variety Demo Modal */}
      {showVarietyDemo && (
        <VarietyDemo onClose={() => setShowVarietyDemo(false)} />
      )}
    </div>
  );
}