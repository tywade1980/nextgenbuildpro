"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

// Define SEO settings type
interface SeoSettings {
  title: string;
  description: string;
  keywords: string[];
  geoTargeting: {
    cities: string[];
    radius: number;
    primaryLocation: string;
  };
  schema: {
    businessType: string;
    includeReviews: boolean;
    includePricing: boolean;
  };
}

// Sample SEO settings - in a real app, this would come from a database
const sampleSeoSettings: SeoSettings = {
  title: "Wade Custom Carpentry | Custom Home Remodeling in Dublin, OH",
  description: "Expert custom carpentry, home remodeling, and renovation services in Dublin and surrounding areas. 25+ years of experience in creating beautiful, functional spaces.",
  keywords: [
    "custom carpentry",
    "home remodeling",
    "kitchen renovation",
    "bathroom remodel",
    "custom closets",
    "built-ins",
    "Dublin Ohio",
    "Columbus carpentry"
  ],
  geoTargeting: {
    cities: ["Dublin", "Powell", "Westerville", "Columbus", "Upper Arlington", "Hilliard"],
    radius: 30,
    primaryLocation: "Dublin, OH"
  },
  schema: {
    businessType: "HomeAndConstructionBusiness",
    includeReviews: true,
    includePricing: false
  }
};

export default function AdminSeo() {
  const [seoSettings, setSeoSettings] = useState<SeoSettings>(sampleSeoSettings);
  const [newKeyword, setNewKeyword] = useState("");
  const [newCity, setNewCity] = useState("");
  const router = useRouter();

  // Auth enforced by middleware (src/middleware.ts)

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    
    if (name.includes('.')) {
      // Handle nested properties
      const [parent, child] = name.split('.');
      setSeoSettings({
        ...seoSettings,
        [parent]: {
          ...seoSettings[parent as keyof SeoSettings],
          [child]: value
        }
      });
    } else {
      // Handle top-level properties
      setSeoSettings({
        ...seoSettings,
        [name]: value
      });
    }
  };

  const handleCheckboxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, checked } = e.target;
    
    if (name.includes('.')) {
      // Handle nested properties
      const [parent, child] = name.split('.');
      setSeoSettings({
        ...seoSettings,
        [parent]: {
          ...seoSettings[parent as keyof SeoSettings],
          [child]: checked
        }
      });
    }
  };

  const handleAddKeyword = () => {
    if (newKeyword.trim() === "") return;
    
    setSeoSettings({
      ...seoSettings,
      keywords: [...seoSettings.keywords, newKeyword.trim()]
    });
    
    setNewKeyword("");
  };

  const handleRemoveKeyword = (index: number) => {
    const updatedKeywords = [...seoSettings.keywords];
    updatedKeywords.splice(index, 1);
    
    setSeoSettings({
      ...seoSettings,
      keywords: updatedKeywords
    });
  };

  const handleAddCity = () => {
    if (newCity.trim() === "") return;
    
    setSeoSettings({
      ...seoSettings,
      geoTargeting: {
        ...seoSettings.geoTargeting,
        cities: [...seoSettings.geoTargeting.cities, newCity.trim()]
      }
    });
    
    setNewCity("");
  };

  const handleRemoveCity = (index: number) => {
    const updatedCities = [...seoSettings.geoTargeting.cities];
    updatedCities.splice(index, 1);
    
    setSeoSettings({
      ...seoSettings,
      geoTargeting: {
        ...seoSettings.geoTargeting,
        cities: updatedCities
      }
    });
  };

  const handleSaveChanges = () => {
    // In a real app, this would save to a database
    alert("SEO settings updated successfully!");
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Admin Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">SEO Settings</h1>
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
              <h3 className="text-lg leading-6 font-medium text-gray-900">Meta Information</h3>
              <p className="mt-1 max-w-2xl text-sm text-gray-500">
                Set your website's title and description for search engines.
              </p>
              
              <div className="mt-6 grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-6">
                <div className="sm:col-span-6">
                  <label htmlFor="title" className="block text-sm font-medium text-gray-700">
                    Page Title
                  </label>
                  <div className="mt-1">
                    <input
                      type="text"
                      name="title"
                      id="title"
                      value={seoSettings.title}
                      onChange={handleInputChange}
                      className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                    />
                  </div>
                  <p className="mt-2 text-sm text-gray-500">
                    Recommended length: 50-60 characters
                  </p>
                </div>

                <div className="sm:col-span-6">
                  <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                    Meta Description
                  </label>
                  <div className="mt-1">
                    <textarea
                      id="description"
                      name="description"
                      rows={3}
                      value={seoSettings.description}
                      onChange={handleInputChange}
                      className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                    />
                  </div>
                  <p className="mt-2 text-sm text-gray-500">
                    Recommended length: 150-160 characters
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-6 bg-white shadow overflow-hidden sm:rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900">Keywords</h3>
              <p className="mt-1 max-w-2xl text-sm text-gray-500">
                Add keywords to help search engines understand your content.
              </p>
              
              <div className="mt-4 flex">
                <input
                  type="text"
                  value={newKeyword}
                  onChange={(e) => setNewKeyword(e.target.value)}
                  placeholder="Add new keyword"
                  className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                />
                <button
                  type="button"
                  onClick={handleAddKeyword}
                  className="ml-3 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-orange-600 hover:bg-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500"
                >
                  Add
                </button>
              </div>
              
              <div className="mt-4">
                <ul className="flex flex-wrap gap-2">
                  {seoSettings.keywords.map((keyword, index) => (
                    <li key={index} className="flex items-center bg-gray-100 px-3 py-1 rounded-full text-sm">
                      <span>{keyword}</span>
                      <button
                        type="button"
                        onClick={() => handleRemoveKeyword(index)}
                        className="ml-2 text-gray-500 hover:text-gray-700"
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

          <div className="mt-6 bg-white shadow overflow-hidden sm:rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900">Geo-Targeting</h3>
              <p className="mt-1 max-w-2xl text-sm text-gray-500">
                Configure location targeting for local search optimization.
              </p>
              
              <div className="mt-6 grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-6">
                <div className="sm:col-span-3">
                  <label htmlFor="geoTargeting.primaryLocation" className="block text-sm font-medium text-gray-700">
                    Primary Location
                  </label>
                  <div className="mt-1">
                    <input
                      type="text"
                      name="geoTargeting.primaryLocation"
                      id="geoTargeting.primaryLocation"
                      value={seoSettings.geoTargeting.primaryLocation}
                      onChange={handleInputChange}
                      className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                    />
                  </div>
                </div>

                <div className="sm:col-span-3">
                  <label htmlFor="geoTargeting.radius" className="block text-sm font-medium text-gray-700">
                    Service Radius (miles)
                  </label>
                  <div className="mt-1">
                    <input
                      type="number"
                      name="geoTargeting.radius"
                      id="geoTargeting.radius"
                      value={seoSettings.geoTargeting.radius}
                      onChange={handleInputChange}
                      className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                    />
                  </div>
                </div>
              </div>
              
              <div className="mt-4">
                <label className="block text-sm font-medium text-gray-700">Service Cities</label>
                <div className="mt-2 flex">
                  <input
                    type="text"
                    value={newCity}
                    onChange={(e) => setNewCity(e.target.value)}
                    placeholder="Add new city"
                    className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                  />
                  <button
                    type="button"
                    onClick={handleAddCity}
                    className="ml-3 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-orange-600 hover:bg-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500"
                  >
                    Add
                  </button>
                </div>
                
                <div className="mt-4">
                  <ul className="grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4">
                    {seoSettings.geoTargeting.cities.map((city, index) => (
                      <li key={index} className="flex items-center justify-between bg-gray-50 px-3 py-2 rounded-md">
                        <span>{city}</span>
                        <button
                          type="button"
                          onClick={() => handleRemoveCity(index)}
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
          </div>

          <div className="mt-6 bg-white shadow overflow-hidden sm:rounded-lg">
            <div className="px-4 py-5 sm:p-6">
              <h3 className="text-lg leading-6 font-medium text-gray-900">Schema Markup</h3>
              <p className="mt-1 max-w-2xl text-sm text-gray-500">
                Configure structured data for better search engine understanding.
              </p>
              
              <div className="mt-6 grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-6">
                <div className="sm:col-span-3">
                  <label htmlFor="schema.businessType" className="block text-sm font-medium text-gray-700">
                    Business Type
                  </label>
                  <div className="mt-1">
                    <select
                      id="schema.businessType"
                      name="schema.businessType"
                      value={seoSettings.schema.businessType}
                      onChange={handleInputChange}
                      className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                    >
                      <option value="HomeAndConstructionBusiness">Home & Construction Business</option>
                      <option value="GeneralContractor">General Contractor</option>
                      <option value="HVACBusiness">HVAC Business</option>
                      <option value="RoofingContractor">Roofing Contractor</option>
                      <option value="LocalBusiness">Local Business</option>
                    </select>
                  </div>
                </div>

                <div className="sm:col-span-6">
                  <div className="flex items-start">
                    <div className="flex items-center h-5">
                      <input
                        id="schema.includeReviews"
                        name="schema.includeReviews"
                        type="checkbox"
                        checked={seoSettings.schema.includeReviews}
                        onChange={handleCheckboxChange}
                        className="focus:ring-orange-500 h-4 w-4 text-orange-600 border-gray-300 rounded"
                      />
                    </div>
                    <div className="ml-3 text-sm">
                      <label htmlFor="schema.includeReviews" className="font-medium text-gray-700">Include Reviews in Schema</label>
                      <p className="text-gray-500">Add review data to structured markup for rich snippets</p>
                    </div>
                  </div>
                </div>

                <div className="sm:col-span-6">
                  <div className="flex items-start">
                    <div className="flex items-center h-5">
                      <input
                        id="schema.includePricing"
                        name="schema.includePricing"
                        type="checkbox"
                        checked={seoSettings.schema.includePricing}
                        onChange={handleCheckboxChange}
                        className="focus:ring-orange-500 h-4 w-4 text-orange-600 border-gray-300 rounded"
                      />
                    </div>
                    <div className="ml-3 text-sm">
                      <label htmlFor="schema.includePricing" className="font-medium text-gray-700">Include Pricing in Schema</label>
                      <p className="text-gray-500">Add pricing information to structured markup</p>
                    </div>
                  </div>
                </div>
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
