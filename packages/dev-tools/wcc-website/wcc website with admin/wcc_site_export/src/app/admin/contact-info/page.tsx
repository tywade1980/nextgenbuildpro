"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

// Define contact info type
interface ContactInfo {
  phone: string;
  email: string;
  serviceAreas: string[];
  socialLinks: {
    facebook: string;
    google: string;
    instagram: string;
  };
}

// Sample contact info - in a real app, this would come from a database
const sampleContactInfo: ContactInfo = {
  phone: "614-359-7218",
  email: "tyler@dublinremodelingservices.net",
  serviceAreas: ["Dublin", "Powell", "Westerville", "Columbus", "Upper Arlington", "Hilliard"],
  socialLinks: {
    facebook: "https://facebook.com/wadecustomcarpentry",
    google: "https://business.google.com/wadecustomcarpentry",
    instagram: "https://instagram.com/wadecustomcarpentry"
  }
};

export default function AdminContactInfo() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [contactInfo, setContactInfo] = useState<ContactInfo>(sampleContactInfo);
  const [newServiceArea, setNewServiceArea] = useState("");
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

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    
    if (name.includes('.')) {
      // Handle nested properties (social links)
      const [parent, child] = name.split('.');
      setContactInfo({
        ...contactInfo,
        [parent]: {
          ...contactInfo[parent as keyof ContactInfo],
          [child]: value
        }
      });
    } else {
      // Handle top-level properties
      setContactInfo({
        ...contactInfo,
        [name]: value
      });
    }
  };

  const handleAddServiceArea = () => {
    if (newServiceArea.trim() === "") return;
    
    setContactInfo({
      ...contactInfo,
      serviceAreas: [...contactInfo.serviceAreas, newServiceArea.trim()]
    });
    
    setNewServiceArea("");
  };

  const handleRemoveServiceArea = (index: number) => {
    const updatedAreas = [...contactInfo.serviceAreas];
    updatedAreas.splice(index, 1);
    
    setContactInfo({
      ...contactInfo,
      serviceAreas: updatedAreas
    });
  };

  const handleSaveChanges = () => {
    // In a real app, this would save to a database
    alert("Contact information updated successfully!");
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
          <h1 className="text-2xl font-bold text-gray-900">Contact Information</h1>
          <Link href="/admin/dashboard" className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300">
            Back to Dashboard
          </Link>
        </div>
      </header>

      {/* Admin Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="bg-white shadow overflow-hidden sm:rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900">Business Contact Information</h3>
              <p className="mt-1 max-w-2xl text-sm text-gray-500">
                Update your contact details and service areas.
              </p>
              
              <div className="mt-6 grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-6">
                <div className="sm:col-span-3">
                  <label htmlFor="phone" className="block text-sm font-medium text-gray-700">
                    Phone Number
                  </label>
                  <div className="mt-1">
                    <input
                      type="text"
                      name="phone"
                      id="phone"
                      value={contactInfo.phone}
                      onChange={handleInputChange}
                      className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                    />
                  </div>
                </div>

                <div className="sm:col-span-3">
                  <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                    Email Address
                  </label>
                  <div className="mt-1">
                    <input
                      type="email"
                      name="email"
                      id="email"
                      value={contactInfo.email}
                      onChange={handleInputChange}
                      className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                    />
                  </div>
                </div>

                <div className="sm:col-span-6">
                  <label htmlFor="socialLinks.facebook" className="block text-sm font-medium text-gray-700">
                    Facebook URL
                  </label>
                  <div className="mt-1">
                    <input
                      type="text"
                      name="socialLinks.facebook"
                      id="socialLinks.facebook"
                      value={contactInfo.socialLinks.facebook}
                      onChange={handleInputChange}
                      className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                    />
                  </div>
                </div>

                <div className="sm:col-span-6">
                  <label htmlFor="socialLinks.google" className="block text-sm font-medium text-gray-700">
                    Google Business URL
                  </label>
                  <div className="mt-1">
                    <input
                      type="text"
                      name="socialLinks.google"
                      id="socialLinks.google"
                      value={contactInfo.socialLinks.google}
                      onChange={handleInputChange}
                      className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                    />
                  </div>
                </div>

                <div className="sm:col-span-6">
                  <label htmlFor="socialLinks.instagram" className="block text-sm font-medium text-gray-700">
                    Instagram URL
                  </label>
                  <div className="mt-1">
                    <input
                      type="text"
                      name="socialLinks.instagram"
                      id="socialLinks.instagram"
                      value={contactInfo.socialLinks.instagram}
                      onChange={handleInputChange}
                      className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-6 bg-white shadow overflow-hidden sm:rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900">Service Areas</h3>
              <p className="mt-1 max-w-2xl text-sm text-gray-500">
                Add or remove locations where you provide services.
              </p>
              
              <div className="mt-4 flex">
                <input
                  type="text"
                  value={newServiceArea}
                  onChange={(e) => setNewServiceArea(e.target.value)}
                  placeholder="Add new service area"
                  className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                />
                <button
                  type="button"
                  onClick={handleAddServiceArea}
                  className="ml-3 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-orange-600 hover:bg-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500"
                >
                  Add
                </button>
              </div>
              
              <div className="mt-4">
                <ul className="grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4">
                  {contactInfo.serviceAreas.map((area, index) => (
                    <li key={index} className="flex items-center justify-between bg-gray-50 px-3 py-2 rounded-md">
                      <span>{area}</span>
                      <button
                        type="button"
                        onClick={() => handleRemoveServiceArea(index)}
                        className="ml-2 text-red-600 hover:text-red-900"
                      >
                        <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                        </svg>
                      </button>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>

          <div className="mt-6 flex justify-end">
            <button
              type="button"
              onClick={handleSaveChanges}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-orange-600 hover:bg-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500"
            >
              Save All Changes
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}
