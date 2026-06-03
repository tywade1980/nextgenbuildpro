"use client";

import React, { useState, useEffect } from 'react';

// --- Placeholder Data ---
// TODO: Replace with actual data fetching/structure later
interface Project {
  id: number;
  title: string;
  category: string;
  location: string;
  imageUrl: string;
  description: string;
}

const placeholderProjects: Project[] = [
  { id: 1, title: "Kitchen Remodel - Dublin", category: "Kitchens", location: "Dublin", imageUrl: "/placeholder-portfolio-1.jpg", description: "Complete kitchen renovation with custom cabinets." },
  { id: 2, title: "Deck Addition - Westerville", category: "Decks", location: "Westerville", imageUrl: "/placeholder-portfolio-2.jpg", description: "New multi-level deck construction." },
  { id: 3, title: "Bathroom Update - Powell", category: "Bathrooms", location: "Powell", imageUrl: "/placeholder-portfolio-3.jpg", description: "Modern bathroom fixtures and tiling." },
  { id: 4, title: "Custom Built-ins - Dublin", category: "Custom Carpentry", location: "Dublin", imageUrl: "/placeholder-portfolio-4.jpg", description: "Living room built-in shelving unit." },
  { id: 5, title: "Screened Porch - Westerville", category: "Decks", location: "Westerville", imageUrl: "/placeholder-portfolio-5.jpg", description: "Conversion of existing porch to screened enclosure." },
  { id: 6, title: "Basement Finishing - Powell", category: "Basements", location: "Powell", imageUrl: "/placeholder-portfolio-6.jpg", description: "Full basement finishing project." },
  // Add more placeholder projects as needed
];

const categories = ["All", "Kitchens", "Bathrooms", "Decks", "Basements", "Custom Carpentry"]; // Example categories
const locations = ["All", "Dublin", "Westerville", "Powell", "Columbus"]; // Example locations
// --- End Placeholder Data ---

export default function PortfolioPage() {
  const [selectedCategory, setSelectedCategory] = useState<string>("All");
  const [selectedLocation, setSelectedLocation] = useState<string>("All");
  const [filteredProjects, setFilteredProjects] = useState<Project[]>(placeholderProjects);

  useEffect(() => {
    let projects = placeholderProjects;

    if (selectedCategory !== "All") {
      projects = projects.filter(p => p.category === selectedCategory);
    }

    if (selectedLocation !== "All") {
      projects = projects.filter(p => p.location === selectedLocation);
    }

    setFilteredProjects(projects);
  }, [selectedCategory, selectedLocation]);

  return (
    <div className="container mx-auto px-6 py-8">
      <h1 className="text-3xl font-bold mb-8 text-center text-gray-800">Our Portfolio</h1>

      {/* Filter Controls */}
      <div className="flex flex-col md:flex-row gap-4 mb-8 justify-center">
        <div>
          <label htmlFor="category-filter" className="block text-sm font-medium text-gray-700 mb-1">Filter by Category:</label>
          <select 
            id="category-filter"
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="block w-full md:w-auto pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-orange-500 focus:border-orange-500 sm:text-sm rounded-md shadow-sm"
          >
            {categories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
          </select>
        </div>
        <div>
          <label htmlFor="location-filter" className="block text-sm font-medium text-gray-700 mb-1">Filter by Location:</label>
          <select 
            id="location-filter"
            value={selectedLocation}
            onChange={(e) => setSelectedLocation(e.target.value)}
            className="block w-full md:w-auto pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-orange-500 focus:border-orange-500 sm:text-sm rounded-md shadow-sm"
          >
            {locations.map(loc => <option key={loc} value={loc}>{loc}</option>)}
          </select>
        </div>
      </div>

      {/* Portfolio Grid */}
      {filteredProjects.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {filteredProjects.map(project => (
            <div key={project.id} className="border rounded-lg overflow-hidden shadow-lg bg-white">
              <div className="relative h-60 w-full bg-gray-300 flex items-center justify-center text-gray-500">
                {/* Placeholder for Image */}
                Image: {project.title}
                {/* <Image src={project.imageUrl} alt={project.title} layout="fill" objectFit="cover" /> */}
              </div>
              <div className="p-4">
                <h3 className="text-lg font-semibold mb-1">{project.title}</h3>
                <p className="text-sm text-gray-500 mb-2">{project.category} | {project.location}</p>
                <p className="text-gray-700 text-sm">{project.description}</p>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-center text-gray-500">No projects found matching your criteria.</p>
      )}
    </div>
  );
}

