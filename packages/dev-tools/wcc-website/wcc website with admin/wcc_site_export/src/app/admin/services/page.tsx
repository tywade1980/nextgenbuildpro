"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

// Define service type
interface Service {
  id: string;
  title: string;
  description: string;
  iconUrl: string;
  linkUrl: string;
  active: boolean;
}

// Sample services data - in a real app, this would come from a database
const sampleServices: Service[] = [
  {
    id: 'home-remodeling',
    title: "Home Remodeling",
    description: "Transform your living spaces with our expert remodeling services, tailored to your style and needs. From complete room renovations to specific area updates, we bring your vision to life with quality craftsmanship.",
    iconUrl: "/icon-home.svg",
    linkUrl: "/services/home-remodeling",
    active: true
  },
  {
    id: 'custom-architecture',
    title: "Custom Architecture",
    description: "Design unique architectural features that enhance your home's functionality and aesthetic appeal. Our custom solutions include built-ins, unique room layouts, and distinctive structural elements that make your space truly yours.",
    iconUrl: "/icon-architecture.svg",
    linkUrl: "/services/custom-architecture",
    active: true
  },
  {
    id: 'molding-trim',
    title: "Molding and Trim Work",
    description: "Add elegance with our precise molding and trim installations for a refined look. Our detailed craftsmanship in crown molding, baseboards, wainscoting, and decorative trim elements elevates any room's appearance.",
    iconUrl: "/icon-trim.svg",
    linkUrl: "/services/molding-trim",
    active: true
  },
  {
    id: 'decorative-enhancements',
    title: "Decorative Enhancements",
    description: "Elevate your home with custom decorative elements that reflect your personal taste and style. From custom shelving and mantels to unique ceiling treatments and architectural details that make your space stand out.",
    iconUrl: "/icon-decorative.svg",
    linkUrl: "/services/decorative-enhancements",
    active: true
  },
  {
    id: 'bathroom-remodeling',
    title: "Bathroom Remodeling",
    description: "Create a modern, functional bathroom with our comprehensive remodeling solutions and expert craftsmanship. We handle everything from layout changes and fixture updates to complete transformations with luxury features.",
    iconUrl: "/icon-bathroom.svg",
    linkUrl: "/services/bathroom-remodeling",
    active: true
  },
  {
    id: 'kitchen-remodeling',
    title: "Kitchen Remodeling",
    description: "Revamp your kitchen with our professional remodeling services, blending style and practicality seamlessly. We create functional, beautiful kitchens with custom cabinetry, quality countertops, and thoughtful layouts.",
    iconUrl: "/icon-kitchen.svg",
    linkUrl: "/services/kitchen-remodeling",
    active: true
  }
];

export default function AdminServices() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [services, setServices] = useState<Service[]>(sampleServices);
  const [editingService, setEditingService] = useState<Service | null>(null);
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

  const toggleServiceActive = (serviceId: string) => {
    setServices(services.map(service => 
      service.id === serviceId 
        ? { ...service, active: !service.active } 
        : service
    ));
  };

  const handleEditService = (service: Service) => {
    setEditingService({...service});
  };

  const handleSaveService = () => {
    if (!editingService) return;
    
    setServices(services.map(service => 
      service.id === editingService.id 
        ? editingService
        : service
    ));
    
    setEditingService(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    if (!editingService) return;
    
    const { name, value } = e.target;
    setEditingService({
      ...editingService,
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
          <h1 className="text-2xl font-bold text-gray-900">Services Management</h1>
          <Link href="/admin/dashboard" className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300">
            Back to Dashboard
          </Link>
        </div>
      </header>

      {/* Admin Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-semibold">Services</h2>
            <button className="px-4 py-2 bg-orange-500 text-white rounded hover:bg-orange-600">
              Add New Service
            </button>
          </div>

          {/* Service List */}
          <div className="bg-white shadow overflow-hidden sm:rounded-md">
            <ul className="divide-y divide-gray-200">
              {services.map((service) => (
                <li key={service.id} className="hover:bg-gray-50">
                  <div className="px-4 py-4 sm:px-6 flex items-center justify-between">
                    <div className="flex items-center">
                      <div className="flex-shrink-0 h-10 w-10 bg-gray-200 rounded-md flex items-center justify-center">
                        <svg className="h-6 w-6 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                        </svg>
                      </div>
                      <div className="ml-4">
                        <div className="text-sm font-medium text-gray-900">{service.title}</div>
                        <div className="text-sm text-gray-500 truncate max-w-md">{service.description.substring(0, 100)}...</div>
                      </div>
                    </div>
                    <div className="flex space-x-2">
                      <button 
                        onClick={() => toggleServiceActive(service.id)}
                        className={`px-3 py-1 rounded text-xs font-medium ${
                          service.active 
                            ? 'bg-green-100 text-green-800' 
                            : 'bg-gray-100 text-gray-800'
                        }`}
                      >
                        {service.active ? 'Active' : 'Inactive'}
                      </button>
                      <button 
                        onClick={() => handleEditService(service)}
                        className="px-3 py-1 bg-blue-100 text-blue-800 rounded text-xs font-medium"
                      >
                        Edit
                      </button>
                      <button className="px-3 py-1 bg-red-100 text-red-800 rounded text-xs font-medium">
                        Delete
                      </button>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </div>

          {/* Edit Service Modal */}
          {editingService && (
            <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center p-4">
              <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Edit Service</h3>
                
                <div className="space-y-4">
                  <div>
                    <label htmlFor="title" className="block text-sm font-medium text-gray-700">Title</label>
                    <input
                      type="text"
                      name="title"
                      id="title"
                      value={editingService.title}
                      onChange={handleInputChange}
                      className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-orange-500 focus:border-orange-500"
                    />
                  </div>
                  
                  <div>
                    <label htmlFor="description" className="block text-sm font-medium text-gray-700">Description</label>
                    <textarea
                      name="description"
                      id="description"
                      rows={4}
                      value={editingService.description}
                      onChange={handleInputChange}
                      className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-orange-500 focus:border-orange-500"
                    />
                  </div>
                  
                  <div>
                    <label htmlFor="linkUrl" className="block text-sm font-medium text-gray-700">Link URL</label>
                    <input
                      type="text"
                      name="linkUrl"
                      id="linkUrl"
                      value={editingService.linkUrl}
                      onChange={handleInputChange}
                      className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-orange-500 focus:border-orange-500"
                    />
                  </div>
                </div>
                
                <div className="mt-6 flex justify-end space-x-3">
                  <button
                    onClick={() => setEditingService(null)}
                    className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSaveService}
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
              <li>Click "Active/Inactive" to toggle whether a service appears on the website</li>
              <li>Use the Edit button to modify service details</li>
              <li>Click "Add New Service" to create a new service offering</li>
              <li>Service icons can be updated in the Edit screen</li>
            </ul>
          </div>
        </div>
      </main>
    </div>
  );
}
