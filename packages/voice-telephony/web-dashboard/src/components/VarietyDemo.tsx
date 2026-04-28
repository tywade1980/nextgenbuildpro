'use client';

import React, { useState } from 'react';
import { ConstructionImageGenerator } from '@/lib/imageGeneration';

interface VarietyDemoProps {
  onClose: () => void;
}

export default function VarietyDemo({ onClose }: VarietyDemoProps) {
  const [currentCombination, setCurrentCombination] = useState(0);
  const generator = new ConstructionImageGenerator();
  const options = generator.getAvailableOptions();
  
  // Create some example combinations to showcase variety
  const exampleCombinations = [
    {
      topic: 'modern skyscraper construction site',
      style: 'architectural rendering',
      lighting: 'golden hour lighting',
      perspective: 'aerial drone view'
    },
    {
      topic: 'bridge building project',
      style: 'technical drawing style',
      lighting: 'bright daylight',
      perspective: 'cross-section view'
    },
    {
      topic: 'heavy machinery and equipment',
      style: 'industrial photography',
      lighting: 'dramatic shadows',
      perspective: 'close-up detail shot'
    },
    {
      topic: 'sustainable building construction',
      style: 'architectural visualization',
      lighting: 'overcast lighting',
      perspective: 'ground level perspective'
    },
    {
      topic: 'construction workers and safety',
      style: 'construction site documentary',
      lighting: 'artificial lighting',
      perspective: 'worker eye level'
    }
  ];

  const totalCombinations = options.topics.length * options.styles.length * options.lighting.length * options.perspectives.length;

  return (
    <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
      <div className="bg-slate-800 rounded-2xl p-8 max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-3xl font-bold text-white">Endless Construction Variety</h2>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white text-2xl"
          >
            ×
          </button>
        </div>

        <div className="mb-8">
          <div className="bg-gradient-to-r from-blue-600 to-cyan-600 p-6 rounded-xl mb-6">
            <h3 className="text-xl font-bold text-white mb-2">🚀 Endless Possibilities</h3>
            <p className="text-blue-100">
              With <strong>{totalCombinations.toLocaleString()}</strong> unique combinations possible, 
              you&apos;ll never run out of construction-themed image ideas!
            </p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            <div className="bg-slate-700 p-4 rounded-lg text-center">
              <div className="text-2xl font-bold text-cyan-400">{options.topics.length}</div>
              <div className="text-sm text-slate-300">Construction Topics</div>
            </div>
            <div className="bg-slate-700 p-4 rounded-lg text-center">
              <div className="text-2xl font-bold text-green-400">{options.styles.length}</div>
              <div className="text-sm text-slate-300">Visual Styles</div>
            </div>
            <div className="bg-slate-700 p-4 rounded-lg text-center">
              <div className="text-2xl font-bold text-yellow-400">{options.lighting.length}</div>
              <div className="text-sm text-slate-300">Lighting Options</div>
            </div>
            <div className="bg-slate-700 p-4 rounded-lg text-center">
              <div className="text-2xl font-bold text-purple-400">{options.perspectives.length}</div>
              <div className="text-sm text-slate-300">Perspectives</div>
            </div>
          </div>
        </div>

        <div className="mb-6">
          <h3 className="text-xl font-bold text-white mb-4">Example Combinations</h3>
          <div className="grid gap-4">
            {exampleCombinations.map((combo, index) => (
              <div
                key={index}
                className={`p-4 rounded-lg border-2 transition-all duration-200 cursor-pointer ${
                  currentCombination === index
                    ? 'border-blue-500 bg-blue-900/30'
                    : 'border-slate-600 bg-slate-700/50 hover:border-slate-500'
                }`}
                onClick={() => setCurrentCombination(index)}
              >
                <div className="grid grid-cols-2 md:grid-cols-4 gap-2 text-sm">
                  <div>
                    <span className="text-slate-400">Topic:</span>
                    <div className="text-cyan-300 font-medium">{combo.topic}</div>
                  </div>
                  <div>
                    <span className="text-slate-400">Style:</span>
                    <div className="text-green-300 font-medium">{combo.style}</div>
                  </div>
                  <div>
                    <span className="text-slate-400">Lighting:</span>
                    <div className="text-yellow-300 font-medium">{combo.lighting}</div>
                  </div>
                  <div>
                    <span className="text-slate-400">Perspective:</span>
                    <div className="text-purple-300 font-medium">{combo.perspective}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="mb-6">
          <h3 className="text-xl font-bold text-white mb-4">Construction Topics Available</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2 text-sm">
            {options.topics.map((topic, index) => (
              <div key={index} className="bg-slate-700 p-2 rounded text-slate-200">
                {topic}
              </div>
            ))}
          </div>
        </div>

        <div className="mb-6">
          <h3 className="text-xl font-bold text-white mb-4">Visual Styles Available</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2 text-sm">
            {options.styles.map((style, index) => (
              <div key={index} className="bg-slate-700 p-2 rounded text-slate-200">
                {style}
              </div>
            ))}
          </div>
        </div>

        <div className="bg-gradient-to-r from-emerald-600 to-teal-600 p-6 rounded-xl">
          <h3 className="text-xl font-bold text-white mb-2">🎯 Key Features</h3>
          <ul className="space-y-2 text-emerald-100">
            <li>• <strong>15 Construction Topics:</strong> From skyscrapers to infrastructure</li>
            <li>• <strong>15 Visual Styles:</strong> Photorealistic to technical drawings</li>
            <li>• <strong>7 Lighting Conditions:</strong> Golden hour to artificial lighting</li>
            <li>• <strong>8 Perspectives:</strong> Aerial views to worker eye-level</li>
            <li>• <strong>Custom Prompts:</strong> Unlimited creative possibilities</li>
            <li>• <strong>Image Modification:</strong> Real-time filters and adjustments</li>
            <li>• <strong>Multiple Formats:</strong> Square, landscape, and portrait</li>
            <li>• <strong>HD Quality:</strong> Professional-grade image generation</li>
          </ul>
        </div>
      </div>
    </div>
  );
}