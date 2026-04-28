"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

// Define portfolio project type
interface PortfolioProject {
  id: string;
  title: string;
  category: string;
  location: string;
  description: string;
  images: string[];
  featured: boolean;
}

// Sample portfolio data - in a real app, this would come from a database
const sampleProjects: PortfolioProject[] = [
  {
    id: 'custom-closet-1',
    title: 'Custom Walk-in Closet',
    category: 'Custom Closets',
    location: 'Dublin, OH',
    description: 'Custom-designed walk-in closet with premium white cabinetry, multiple drawers, hanging spaces, and specialized storage solutions.',
    images: ['/placeholder-closet-1.jpg', '/placeholder-closet-2.jpg'],
    featured: true
  },
  {
    id: 'mudroom-1',
    title: 'Entryway Mudroom Built-ins',
    category: 'Built-ins',
    location: 'Powell, OH',
    description: 'Functional entryway mudroom with custom built-in storage, bench seating, and coat hooks for organization.',
    images: ['/placeholder-mudroom-1.jpg'],
    featured: true
  },
  {
    id: 'bathroom-1',
    title: 'Modern Bathroom Renovation',
    category: 'Bathroom Remodeling',
    location: 'Westerville, OH',
    description: 'Complete bathroom renovation featuring custom tile work, modern fixtures, and elegant finishes.',
    images: ['/placeholder-bathroom-1.jpg', '/placeholder-bathroom-2.jpg'],
    featured: false
  },
  {
    id: 'outdoor-1',
    title: 'Backyard Oasis with Pergola',
    category: 'Outdoor Living',
    location: 'Dublin, OH',
    description: 'Custom outdoor living space featuring a white pergola, composite decking, and integrated seating areas.',
    images: ['/placeholder-outdoor-1.jpg'],
    featured: true
  },
  {
    id: 'landscape-1',
    title: 'Backyard Pond & Patio',
    category: 'Landscaping',
    location: 'Columbus, OH',
    description: 'Integrated hardscaping project with paver patio, natural stone fire pit, and decorative pond with water features.',
    images: ['/placeholder-landscape-1.jpg', '/placeholder-landscape-2.jpg'],
    featured: false
  }
];

export default function AdminPortfolio() {
  const [projects, setProjects] = useState<PortfolioProject[]>(sampleProjects);
  const [draggedProject, setDraggedProject] = useState<string | null>(null);
  const router = useRouter();

  // Auth enforced by middleware (src/middleware.ts)

  const handleDragStart = (projectId: string) => {
    setDraggedProject(projectId);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleDrop = (targetIndex: number) => {
    if (draggedProject === null) return;
    
    const draggedIndex = projects.findIndex(p => p.id === draggedProject);
    if (draggedIndex === -1) return;
    
    // Create a new array with the dragged item moved to the target position
    const newProjects = [...projects];
    const [removed] = newProjects.splice(draggedIndex, 1);
    newProjects.splice(targetIndex, 0, removed);
    
    setProjects(newProjects);
    setDraggedProject(null);
  };

  const toggleFeatured = (projectId: string) => {
    setProjects(projects.map(project => 
      project.id === projectId 
        ? { ...project, featured: !project.featured } 
        : project
    ));
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Admin Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">Portfolio Management</h1>
          <Link href="/admin/dashboard" className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300">
            Back to Dashboard
          </Link>
        </div>
      </header>

      {/* Admin Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-semibold">Portfolio Projects</h2>
            <button className="px-4 py-2 bg-orange-500 text-white rounded hover:bg-orange-600">
              Add New Project
            </button>
          </div>

          {/* Project List */}
          <div className="bg-white shadow overflow-hidden sm:rounded-md">
            <ul className="divide-y divide-gray-200">
              {projects.map((project, index) => (
                <li 
                  key={project.id}
                  draggable
                  onDragStart={() => handleDragStart(project.id)}
                  onDragOver={handleDragOver}
                  onDrop={() => handleDrop(index)}
                  className={`hover:bg-gray-50 cursor-move ${draggedProject === project.id ? 'opacity-50 bg-gray-100' : ''}`}
                >
                  <div className="px-4 py-4 sm:px-6 flex items-center justify-between">
                    <div className="flex items-center">
                      <div className="flex-shrink-0 h-10 w-10 bg-gray-200 rounded-md flex items-center justify-center">
                        <svg className="h-6 w-6 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h7" />
                        </svg>
                      </div>
                      <div className="ml-4">
                        <div className="text-sm font-medium text-gray-900">{project.title}</div>
                        <div className="text-sm text-gray-500">{project.category} • {project.location}</div>
                      </div>
                    </div>
                    <div className="flex space-x-2">
                      <button 
                        onClick={() => toggleFeatured(project.id)}
                        className={`px-3 py-1 rounded text-xs font-medium ${
                          project.featured 
                            ? 'bg-yellow-100 text-yellow-800' 
                            : 'bg-gray-100 text-gray-800'
                        }`}
                      >
                        {project.featured ? 'Featured' : 'Not Featured'}
                      </button>
                      <Link 
                        href={`/admin/portfolio/edit/${project.id}`}
                        className="px-3 py-1 bg-blue-100 text-blue-800 rounded text-xs font-medium"
                      >
                        Edit
                      </Link>
                      <button className="px-3 py-1 bg-red-100 text-red-800 rounded text-xs font-medium">
                        Delete
                      </button>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </div>

          <div className="mt-6 bg-white p-4 rounded-md shadow">
            <h3 className="text-lg font-medium mb-4">Instructions</h3>
            <ul className="list-disc pl-5 space-y-2 text-sm text-gray-600">
              <li>Drag and drop projects to reorder them</li>
              <li>Click "Featured" to toggle whether a project appears on the homepage</li>
              <li>Use the Edit button to modify project details and images</li>
              <li>Click "Add New Project" to create a new portfolio entry</li>
            </ul>
          </div>
        </div>
      </main>
    </div>
  );
}
