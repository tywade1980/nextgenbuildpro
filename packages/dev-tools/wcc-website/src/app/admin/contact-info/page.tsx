'use client';

import { useState } from 'react';

interface ContactInfo {
  phone: string;
  email: string;
  address: string;
  serviceAreas: string[];
  socialLinks: {
    facebook: string;
    instagram: string;
    linkedin: string;
  };
}

export default function ContactInfoPage() {
  const [contactInfo, setContactInfo] = useState<ContactInfo>({
    phone: '(614) 359-7218',
    email: 'tyler@dublinremodelingservices.net',
    address: 'Dublin, Ohio',
    serviceAreas: ['Dublin', 'Columbus', 'Hilliard', 'Upper Arlington'],
    socialLinks: {
      facebook: '',
      instagram: '',
      linkedin: ''
    }
  });

  const [newServiceArea, setNewServiceArea] = useState('');

  const handleInputChange = (name: string, value: string) => {
    if (name.includes('.')) {
      // Handle nested properties (social links)
      const [parent, child] = name.split('.');
      setContactInfo(prev => ({
        ...prev,
        [parent]: {
          ...(prev[parent as keyof ContactInfo] as any),
          [child]: value
        }
      }));
    } else {
      // Handle top-level properties
      setContactInfo(prev => ({
        ...prev,
        [name]: value
      }));
    }
  };

  const handleAddServiceArea = () => {
    if (newServiceArea.trim() === '') return;
    
    setContactInfo(prev => ({
      ...prev,
      serviceAreas: [...prev.serviceAreas, newServiceArea]
    }));
    setNewServiceArea('');
  };

  const handleRemoveServiceArea = (index: number) => {
    setContactInfo(prev => ({
      ...prev,
      serviceAreas: prev.serviceAreas.filter((_, i) => i !== index)
    }));
  };

  const handleSave = () => {
    // In a real application, this would save to a database
    console.log('Saving contact info:', contactInfo);
    alert('Contact information saved successfully!');
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <div className="bg-white rounded-lg shadow-md p-6">
          <h1 className="text-3xl font-bold text-gray-800 mb-6">Contact Information Management</h1>
          
          <div className="space-y-6">
            {/* Basic Contact Info */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Phone Number
                </label>
                <input
                  type="tel"
                  value={contactInfo.phone}
                  onChange={(e) => handleInputChange('phone', e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Email Address
                </label>
                <input
                  type="email"
                  value={contactInfo.email}
                  onChange={(e) => handleInputChange('email', e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Business Address
                </label>
                <input
                  type="text"
                  value={contactInfo.address}
                  onChange={(e) => handleInputChange('address', e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>

            {/* Service Areas */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Service Areas
              </label>
              <div className="flex flex-wrap gap-2 mb-3">
                {contactInfo.serviceAreas.map((area, index) => (
                  <span
                    key={index}
                    className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-blue-100 text-blue-800"
                  >
                    {area}
                    <button
                      onClick={() => handleRemoveServiceArea(index)}
                      className="ml-2 text-blue-600 hover:text-blue-800"
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
              <div className="flex gap-2">
                <input
                  type="text"
                  value={newServiceArea}
                  onChange={(e) => setNewServiceArea(e.target.value)}
                  placeholder="Add new service area"
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  onClick={handleAddServiceArea}
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                >
                  Add
                </button>
              </div>
            </div>

            {/* Social Links */}
            <div>
              <h3 className="text-lg font-medium text-gray-800 mb-4">Social Media Links</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Facebook
                  </label>
                  <input
                    type="url"
                    value={contactInfo.socialLinks.facebook}
                    onChange={(e) => handleInputChange('socialLinks.facebook', e.target.value)}
                    placeholder="https://facebook.com/yourpage"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Instagram
                  </label>
                  <input
                    type="url"
                    value={contactInfo.socialLinks.instagram}
                    onChange={(e) => handleInputChange('socialLinks.instagram', e.target.value)}
                    placeholder="https://instagram.com/yourpage"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    LinkedIn
                  </label>
                  <input
                    type="url"
                    value={contactInfo.socialLinks.linkedin}
                    onChange={(e) => handleInputChange('socialLinks.linkedin', e.target.value)}
                    placeholder="https://linkedin.com/company/yourcompany"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
            </div>

            {/* Save Button */}
            <div className="flex justify-end">
              <button
                onClick={handleSave}
                className="px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500"
              >
                Save Changes
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
