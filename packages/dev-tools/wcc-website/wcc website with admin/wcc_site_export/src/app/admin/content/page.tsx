"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

// Define content section type
interface ContentSection {
  id: string;
  name: string;
  title: string;
  content: string;
  location: string;
}

// Sample content data - in a real app, this would come from a database
const sampleContent: ContentSection[] = [
  {
    id: 'hero',
    name: 'Hero Section',
    title: 'Crafting Dreams, Building Homes',
    content: 'With 25 years of expertise, Wade Custom Carpentry specializes in transforming homes with custom remodeling, architecture, and decorative craftsmanship.',
    location: 'Homepage'
  },
  {
    id: 'about',
    name: 'About Us',
    title: 'Bringing Expertise and Passion to Every Project',
    content: 'At Wade Custom Carpentry, we bring over 25 years of experience to every project, ensuring top-quality craftsmanship and personalized service. Led by Tyler, our skilled team is dedicated to transforming your home with precision and creativity. From custom remodeling and architectural work to intricate molding and decorative details, we handle every job with care and expertise. Our commitment to excellence and customer satisfaction makes us the ideal choice for your next home improvement project.',
    location: 'Homepage'
  },
  {
    id: 'services-intro',
    name: 'Services Introduction',
    title: 'Expert Services for Your Home',
    content: 'Our services include expert home remodeling, custom architecture, detailed molding, and more. With 25 years of experience, we deliver high-quality results tailored to your specific needs.',
    location: 'Homepage'
  },
  {
    id: 'gallery-intro',
    name: 'Gallery Introduction',
    title: 'Showcasing Our Craftsmanship and Projects',
    content: 'Explore our gallery to see the high-quality craftsmanship and creative designs we\'ve brought to life. Each project highlights our commitment to excellence and attention to detail.',
    location: 'Homepage'
  },
  {
    id: 'contact-intro',
    name: 'Contact Introduction',
    title: 'Reach out for personalized service and expert advice on your project.',
    content: '',
    location: 'Homepage'
  },
  {
    id: 'discount',
    name: 'Special Discount',
    title: '5% Senior & Military Discount',
    content: 'Enjoy a 5% discount on all services as a token of our appreciation for seniors and military members.',
    location: 'Homepage'
  }
];

export default function AdminContent() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [contentSections, setContentSections] = useState<ContentSection[]>(sampleContent);
  const [editingSection, setEditingSection] = useState<ContentSection | null>(null);
  const router = useRouter();

  useEffect(() => {
    // Check if user is authenticated
    const authStatus = localStorage.getItem('wcc_admin_auth');
    if (authStatus !== 'true') {
      router.push('/admin');
    } else {
      setIsAuthenticated(true);
    }
    setIsLoading(false);
  }, [router]);

  const handleEditSection = (section: ContentSection) => {
    setEditingSection({...section});
  };

  const handleSaveSection = () => {
    if (!editingSection) return;
    
    setContentSections(contentSections.map(section => 
      section.id === editingSection.id 
        ? editingSection
        : section
    ));
    
    setEditingSection(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    if (!editingSection) return;
    
    const { name, value } = e.target;
    setEditingSection({
      ...editingSection,
      [name]: value
    });
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <p className="text-gray-600">Loading...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null; // Will redirect in useEffect
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Admin Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">Content Management</h1>
          <Link href="/admin/dashboard" className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300">
            Back to Dashboard
          </Link>
        </div>
      </header>

      {/* Admin Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-semibold">Website Content Sections</h2>
            <button className="px-4 py-2 bg-orange-500 text-white rounded hover:bg-orange-600">
              Add New Section
            </button>
          </div>

          {/* Content Sections List */}
          <div className="bg-white shadow overflow-hidden sm:rounded-md">
            <ul className="divide-y divide-gray-200">
              {contentSections.map((section) => (
                <li key={section.id} className="hover:bg-gray-50">
                  <div className="px-4 py-4 sm:px-6">
                    <div className="flex items-center justify-between">
                      <div>
                        <h3 className="text-sm font-medium text-gray-900">{section.name}</h3>
                        <p className="mt-1 text-sm text-gray-500">{section.location}</p>
                      </div>
                      <div>
                        <button 
                          onClick={() => handleEditSection(section)}
                          className="px-3 py-1 bg-blue-100 text-blue-800 rounded text-xs font-medium"
                        >
                          Edit
                        </button>
                      </div>
                    </div>
                    <div className="mt-2">
                      <p className="text-sm font-medium text-gray-800">{section.title}</p>
                      <p className="mt-1 text-sm text-gray-600 line-clamp-2">{section.content}</p>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </div>

          {/* Edit Content Modal */}
          {editingSection && (
            <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Edit Content Section</h3>
                
                <div className="space-y-4">
                  <div>
                    <label htmlFor="name" className="block text-sm font-medium text-gray-700">Section Name</label>
                    <input
                      type="text"
                      name="name"
                      id="name"
                      value={editingSection.name}
                      onChange={handleInputChange}
                      className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-orange-500 focus:border-orange-500"
                    />
                  </div>
                  
                  <div>
                    <label htmlFor="title" className="block text-sm font-medium text-gray-700">Title</label>
                    <input
                      type="text"
                      name="title"
                      id="title"
                      value={editingSection.title}
                      onChange={handleInputChange}
                      className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-orange-500 focus:border-orange-500"
                    />
                  </div>
                  
                  <div>
                    <label htmlFor="content" className="block text-sm font-medium text-gray-700">Content</label>
                    <textarea
                      name="content"
                      id="content"
                      rows={6}
                      value={editingSection.content}
                      onChange={handleInputChange}
                      className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-orange-500 focus:border-orange-500"
                    />
                  </div>
                  
                  <div>
                    <label htmlFor="location" className="block text-sm font-medium text-gray-700">Location</label>
                    <input
                      type="text"
                      name="location"
                      id="location"
                      value={editingSection.location}
                      onChange={handleInputChange}
                      className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-orange-500 focus:border-orange-500"
                    />
                  </div>
                </div>
                
                <div className="mt-6 flex justify-end space-x-3">
                  <button
                    onClick={() => setEditingSection(null)}
                    className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSaveSection}
                    className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-orange-600 hover:bg-orange-700"
                  >
                    Save Changes
                  </button>
                </div>
              </div>
            </div>
          )}

          <div className="mt-6 bg-white p-4 rounded-md shadow">
            <h3 className="text-lg font-medium mb-4">Instructions</h3>
            <ul className="list-disc pl-5 space-y-2 text-sm text-gray-600">
              <li>Click "Edit" to modify content section text</li>
              <li>Changes will be reflected immediately on the website</li>
              <li>Use the formatting options in the editor for rich text formatting</li>
              <li>Click "Add New Section" to create additional content areas</li>
            </ul>
          </div>
        </div>
      </main>
    </div>
  );
}
