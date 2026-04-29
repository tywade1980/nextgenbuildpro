"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

// Define lead type
interface Lead {
  id: string;
  name: string;
  email: string;
  phone: string;
  message: string;
  projectType: string;
  status: 'new' | 'contacted' | 'qualified' | 'converted' | 'lost';
  dateReceived: string;
  notes: string;
}

// Sample leads data - in a real app, this would come from a database
const sampleLeads: Lead[] = [
  {
    id: 'lead-001',
    name: 'John Smith',
    email: 'john.smith@example.com',
    phone: '614-555-1234',
    message: 'I\'m interested in a custom closet installation for our master bedroom.',
    projectType: 'Custom Closet',
    status: 'new',
    dateReceived: '2025-06-01T14:32:00',
    notes: ''
  },
  {
    id: 'lead-002',
    name: 'Sarah Johnson',
    email: 'sarah.j@example.com',
    phone: '614-555-5678',
    message: 'We need to remodel our kitchen. Looking for someone who can help with custom cabinetry and countertops.',
    projectType: 'Kitchen Remodeling',
    status: 'contacted',
    dateReceived: '2025-05-28T09:15:00',
    notes: 'Called on 5/29, scheduled initial consultation for 6/10'
  },
  {
    id: 'lead-003',
    name: 'Michael Brown',
    email: 'mbrown@example.com',
    phone: '614-555-9012',
    message: 'I\'d like to get a quote for building a deck in my backyard.',
    projectType: 'Outdoor Living',
    status: 'qualified',
    dateReceived: '2025-05-25T16:45:00',
    notes: 'Site visit completed 5/30, preparing quote'
  },
  {
    id: 'lead-004',
    name: 'Emily Davis',
    email: 'emily.davis@example.com',
    phone: '614-555-3456',
    message: 'Looking for someone to install built-in bookshelves in my home office.',
    projectType: 'Built-ins',
    status: 'converted',
    dateReceived: '2025-05-20T11:20:00',
    notes: 'Project started 6/5, estimated completion 6/15'
  },
  {
    id: 'lead-005',
    name: 'Robert Wilson',
    email: 'rwilson@example.com',
    phone: '614-555-7890',
    message: 'Need bathroom renovation. Complete gut and remodel.',
    projectType: 'Bathroom Remodeling',
    status: 'lost',
    dateReceived: '2025-05-15T13:10:00',
    notes: 'Went with another contractor due to scheduling conflicts'
  }
];

export default function AdminLeads() {
  const [leads, setLeads] = useState<Lead[]>(sampleLeads);
  const [selectedLead, setSelectedLead] = useState<Lead | null>(null);
  const [filter, setFilter] = useState<string>('all');
  const router = useRouter();

  // Auth enforced by middleware (src/middleware.ts)

  const filteredLeads = filter === 'all' 
    ? leads 
    : leads.filter(lead => lead.status === filter);

  const handleStatusChange = (leadId: string, newStatus: Lead['status']) => {
    setLeads(leads.map(lead => 
      lead.id === leadId 
        ? { ...lead, status: newStatus } 
        : lead
    ));
    
    if (selectedLead && selectedLead.id === leadId) {
      setSelectedLead({ ...selectedLead, status: newStatus });
    }
  };

  const handleNoteChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    if (!selectedLead) return;
    
    const updatedLead = { ...selectedLead, notes: e.target.value };
    setSelectedLead(updatedLead);
    
    setLeads(leads.map(lead => 
      lead.id === selectedLead.id 
        ? updatedLead
        : lead
    ));
  };

  const getStatusColor = (status: Lead['status']) => {
    switch (status) {
      case 'new': return 'bg-blue-100 text-blue-800';
      case 'contacted': return 'bg-yellow-100 text-yellow-800';
      case 'qualified': return 'bg-purple-100 text-purple-800';
      case 'converted': return 'bg-green-100 text-green-800';
      case 'lost': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Admin Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">Lead Management</h1>
          <Link href="/admin/dashboard" className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300">
            Back to Dashboard
          </Link>
        </div>
      </header>

      {/* Admin Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h2 className="text-xl font-semibold">Customer Leads</h2>
              <p className="text-sm text-gray-500">Manage and track potential customers</p>
            </div>
            <div className="flex items-center space-x-2">
              <span className="text-sm text-gray-700">Filter:</span>
              <select
                value={filter}
                onChange={(e) => setFilter(e.target.value)}
                className="border-gray-300 rounded-md shadow-sm focus:ring-orange-500 focus:border-orange-500 sm:text-sm"
              >
                <option value="all">All Leads</option>
                <option value="new">New</option>
                <option value="contacted">Contacted</option>
                <option value="qualified">Qualified</option>
                <option value="converted">Converted</option>
                <option value="lost">Lost</option>
              </select>
            </div>
          </div>

          <div className="flex flex-col lg:flex-row space-y-6 lg:space-y-0 lg:space-x-6">
            {/* Leads List */}
            <div className="lg:w-2/3">
              <div className="bg-white shadow overflow-hidden sm:rounded-md">
                <ul className="divide-y divide-gray-200">
                  {filteredLeads.length > 0 ? (
                    filteredLeads.map((lead) => (
                      <li 
                        key={lead.id} 
                        className={`hover:bg-gray-50 cursor-pointer ${selectedLead?.id === lead.id ? 'bg-gray-50' : ''}`}
                        onClick={() => setSelectedLead(lead)}
                      >
                        <div className="px-4 py-4 sm:px-6">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center">
                              <p className="text-sm font-medium text-gray-900 truncate">{lead.name}</p>
                              <span className={`ml-2 px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(lead.status)}`}>
                                {lead.status.charAt(0).toUpperCase() + lead.status.slice(1)}
                              </span>
                            </div>
                            <div className="ml-2 flex-shrink-0 flex">
                              <p className="text-sm text-gray-500">{formatDate(lead.dateReceived)}</p>
                            </div>
                          </div>
                          <div className="mt-2 sm:flex sm:justify-between">
                            <div className="sm:flex">
                              <p className="flex items-center text-sm text-gray-500">
                                <svg className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                                </svg>
                                {lead.email}
                              </p>
                              <p className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0 sm:ml-6">
                                <svg className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                                </svg>
                                {lead.phone}
                              </p>
                            </div>
                            <div className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
                              <svg className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                              </svg>
                              {lead.projectType}
                            </div>
                          </div>
                        </div>
                      </li>
                    ))
                  ) : (
                    <li className="px-4 py-6 text-center text-gray-500">
                      No leads found matching the selected filter.
                    </li>
                  )}
                </ul>
              </div>
            </div>

            {/* Lead Details */}
            <div className="lg:w-1/3">
              {selectedLead ? (
                <div className="bg-white shadow overflow-hidden sm:rounded-lg">
                  <div className="px-4 py-5 sm:px-6">
                    <h3 className="text-lg leading-6 font-medium text-gray-900">Lead Details</h3>
                    <p className="mt-1 max-w-2xl text-sm text-gray-500">
                      Personal details and project information.
                    </p>
                  </div>
                  <div className="border-t border-gray-200 px-4 py-5 sm:p-0">
                    <dl className="sm:divide-y sm:divide-gray-200">
                      <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                        <dt className="text-sm font-medium text-gray-500">Full name</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{selectedLead.name}</dd>
                      </div>
                      <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                        <dt className="text-sm font-medium text-gray-500">Email address</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{selectedLead.email}</dd>
                      </div>
                      <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                        <dt className="text-sm font-medium text-gray-500">Phone number</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{selectedLead.phone}</dd>
                      </div>
                      <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                        <dt className="text-sm font-medium text-gray-500">Project type</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{selectedLead.projectType}</dd>
                      </div>
                      <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                        <dt className="text-sm font-medium text-gray-500">Date received</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{formatDate(selectedLead.dateReceived)}</dd>
                      </div>
                      <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                        <dt className="text-sm font-medium text-gray-500">Status</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                          <select
                            value={selectedLead.status}
                            onChange={(e) => handleStatusChange(selectedLead.id, e.target.value as Lead['status'])}
                            className="border-gray-300 rounded-md shadow-sm focus:ring-orange-500 focus:border-orange-500 sm:text-sm"
                          >
                            <option value="new">New</option>
                            <option value="contacted">Contacted</option>
                            <option value="qualified">Qualified</option>
                            <option value="converted">Converted</option>
                            <option value="lost">Lost</option>
                          </select>
                        </dd>
                      </div>
                      <div className="py-4 sm:py-5 sm:px-6">
                        <dt className="text-sm font-medium text-gray-500 mb-2">Message</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0 bg-gray-50 p-3 rounded-md">
                          {selectedLead.message}
                        </dd>
                      </div>
                      <div className="py-4 sm:py-5 sm:px-6">
                        <dt className="text-sm font-medium text-gray-500 mb-2">Notes</dt>
                        <dd className="mt-1 text-sm text-gray-900 sm:mt-0">
                          <textarea
                            rows={4}
                            value={selectedLead.notes}
                            onChange={handleNoteChange}
                            className="shadow-sm focus:ring-orange-500 focus:border-orange-500 block w-full sm:text-sm border-gray-300 rounded-md"
                            placeholder="Add notes about this lead..."
                          />
                        </dd>
                      </div>
                    </dl>
                  </div>
                  <div className="px-4 py-3 bg-gray-50 text-right sm:px-6">
                    <button
                      type="button"
                      className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-orange-600 hover:bg-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500"
                    >
                      Save Changes
                    </button>
                  </div>
                </div>
              ) : (
                <div className="bg-white shadow overflow-hidden sm:rounded-lg p-6 text-center">
                  <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                  <h3 className="mt-2 text-sm font-medium text-gray-900">No lead selected</h3>
                  <p className="mt-1 text-sm text-gray-500">
                    Select a lead from the list to view details.
                  </p>
                </div>
              )}
            </div>
          </div>

          <div className="mt-6 bg-white p-4 rounded-md shadow">
            <h3 className="text-lg font-medium mb-4">Lead Management Instructions</h3>
            <ul className="list-disc pl-5 space-y-2 text-sm text-gray-600">
              <li>Click on a lead to view and edit its details</li>
              <li>Update lead status as you progress through your sales process</li>
              <li>Add notes to keep track of communications and next steps</li>
              <li>Use the filter dropdown to focus on specific lead statuses</li>
              <li>All leads from the contact form will automatically appear here</li>
            </ul>
          </div>
        </div>
      </main>
    </div>
  );
}
