'use client'

import Link from "next/link";
import { useState } from "react";
import { Users, ArrowLeft, Mail, Phone, MapPin, Building, Calendar, Plus } from "lucide-react";

interface Client {
  id: string;
  name: string;
  email: string;
  phone?: string;
  address?: string;
  createdAt: string;
  _count: {
    projects: number;
    calls: number;
  };
}

// Mock data for demonstration
const mockClients: Client[] = [
  {
    id: '1',
    name: 'ABC Construction Corp',
    email: 'contact@abcconstruction.com',
    phone: '+1-555-0123',
    address: '123 Builder St, Construction City, CC 12345',
    createdAt: '2023-01-15T00:00:00Z',
    _count: {
      projects: 3,
      calls: 12
    }
  },
  {
    id: '2',
    name: 'XYZ Development LLC',
    email: 'info@xyzdevelopment.com',
    phone: '+1-555-0456',
    address: '456 Developer Ave, Building Town, BT 67890',
    createdAt: '2023-03-20T00:00:00Z',
    _count: {
      projects: 2,
      calls: 8
    }
  },
  {
    id: '3',
    name: 'Retail Properties Inc',
    email: 'projects@retailproperties.com',
    phone: '+1-555-0789',
    address: '789 Commercial Blvd, Retail District, RD 11111',
    createdAt: '2023-05-10T00:00:00Z',
    _count: {
      projects: 1,
      calls: 5
    }
  },
  {
    id: '4',
    name: 'Logistics Solutions Ltd',
    email: 'construction@logisticssolutions.com',
    phone: '+1-555-0321',
    address: '321 Warehouse Way, Industrial Zone, IZ 22222',
    createdAt: '2023-06-01T00:00:00Z',
    _count: {
      projects: 4,
      calls: 15
    }
  },
  {
    id: '5',
    name: 'Green Energy Consortium',
    email: 'facilities@greenenergy.org',
    phone: '+1-555-0654',
    address: '654 Solar Street, Renewable City, RC 33333',
    createdAt: '2023-08-15T00:00:00Z',
    _count: {
      projects: 2,
      calls: 6
    }
  }
];

export default function ClientsPage() {
  const [clients] = useState<Client[]>(mockClients);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedClient, setSelectedClient] = useState<Client | null>(null);

  const filteredClients = clients.filter(client =>
    client.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    client.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const totalProjects = clients.reduce((sum, client) => sum + client._count.projects, 0);
  const totalCalls = clients.reduce((sum, client) => sum + client._count.calls, 0);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Link href="/" className="flex items-center text-gray-600 hover:text-gray-900">
                <ArrowLeft className="h-5 w-5 mr-2" />
                Back to Dashboard
              </Link>
            </div>
            <div className="flex items-center">
              <Users className="h-8 w-8 text-blue-600" />
              <h1 className="ml-2 text-xl font-bold text-gray-900">Client Management</h1>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h2 className="text-3xl font-bold text-gray-900">Client Management</h2>
            <p className="text-gray-600 mt-2">Manage your construction business clients and their project relationships</p>
          </div>
          <button className="bg-blue-600 text-white px-4 py-2 rounded-lg flex items-center hover:bg-blue-700">
            <Plus className="h-5 w-5 mr-2" />
            Add Client
          </button>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Users className="h-8 w-8 text-blue-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Clients</p>
                <p className="text-2xl font-bold text-gray-900">{clients.length}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Building className="h-8 w-8 text-green-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Active Projects</p>
                <p className="text-2xl font-bold text-gray-900">{totalProjects}</p>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Phone className="h-8 w-8 text-purple-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total Calls</p>
                <p className="text-2xl font-bold text-gray-900">{totalCalls}</p>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center">
              <Calendar className="h-8 w-8 text-yellow-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Avg. Relationship</p>
                <p className="text-2xl font-bold text-gray-900">8 mo</p>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Client List */}
          <div className="lg:col-span-2">
            {/* Search */}
            <div className="mb-6">
              <div className="relative">
                <input
                  type="text"
                  placeholder="Search clients by name or email..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <Users className="absolute left-3 top-2.5 h-5 w-5 text-gray-400" />
              </div>
            </div>

            {/* Client Cards */}
            <div className="space-y-4">
              {filteredClients.map((client) => (
                <div 
                  key={client.id} 
                  className={`bg-white rounded-lg shadow p-6 cursor-pointer hover:shadow-md transition-shadow ${
                    selectedClient?.id === client.id ? 'ring-2 ring-blue-500' : ''
                  }`}
                  onClick={() => setSelectedClient(client)}
                >
                  <div className="flex justify-between items-start mb-4">
                    <div>
                      <h3 className="text-lg font-semibold text-gray-900 mb-1">{client.name}</h3>
                      <p className="text-sm text-gray-600">Client since {formatDate(client.createdAt)}</p>
                    </div>
                    <Users className="h-6 w-6 text-gray-400" />
                  </div>

                  <div className="space-y-2 mb-4">
                    <div className="flex items-center text-sm text-gray-600">
                      <Mail className="h-4 w-4 mr-2" />
                      <span>{client.email}</span>
                    </div>

                    {client.phone && (
                      <div className="flex items-center text-sm text-gray-600">
                        <Phone className="h-4 w-4 mr-2" />
                        <span>{client.phone}</span>
                      </div>
                    )}

                    {client.address && (
                      <div className="flex items-center text-sm text-gray-600">
                        <MapPin className="h-4 w-4 mr-2" />
                        <span className="truncate">{client.address}</span>
                      </div>
                    )}
                  </div>

                  <div className="flex justify-between items-center pt-4 border-t border-gray-200">
                    <div className="flex space-x-4 text-sm text-gray-600">
                      <div className="flex items-center">
                        <Building className="h-4 w-4 mr-1" />
                        <span>{client._count.projects} projects</span>
                      </div>
                      <div className="flex items-center">
                        <Phone className="h-4 w-4 mr-1" />
                        <span>{client._count.calls} calls</span>
                      </div>
                    </div>
                    <div className="flex space-x-2">
                      <button className="text-blue-600 hover:text-blue-800 text-sm font-medium">
                        View Projects
                      </button>
                      <button className="text-gray-600 hover:text-gray-800 text-sm font-medium">
                        Edit
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {filteredClients.length === 0 && (
              <div className="text-center py-12">
                <Users className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">No clients found</h3>
                <p className="text-gray-600">
                  {searchTerm ? 'Try adjusting your search terms' : 'Add your first client to get started'}
                </p>
              </div>
            )}
          </div>

          {/* Client Details Panel */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow p-6 sticky top-8">
              {selectedClient ? (
                <div>
                  <div className="flex items-center mb-4">
                    <Users className="h-6 w-6 text-blue-600" />
                    <h3 className="ml-2 text-lg font-semibold text-gray-900">Client Details</h3>
                  </div>

                  <div className="space-y-4">
                    <div>
                      <label className="text-sm font-medium text-gray-600">Company Name</label>
                      <p className="text-gray-900 font-medium">{selectedClient.name}</p>
                    </div>

                    <div>
                      <label className="text-sm font-medium text-gray-600">Email</label>
                      <p className="text-gray-900">{selectedClient.email}</p>
                    </div>

                    {selectedClient.phone && (
                      <div>
                        <label className="text-sm font-medium text-gray-600">Phone</label>
                        <p className="text-gray-900">{selectedClient.phone}</p>
                      </div>
                    )}

                    {selectedClient.address && (
                      <div>
                        <label className="text-sm font-medium text-gray-600">Address</label>
                        <p className="text-gray-900 text-sm">{selectedClient.address}</p>
                      </div>
                    )}

                    <div>
                      <label className="text-sm font-medium text-gray-600">Client Since</label>
                      <p className="text-gray-900">{formatDate(selectedClient.createdAt)}</p>
                    </div>

                    <div className="pt-4 border-t border-gray-200">
                      <h4 className="text-sm font-medium text-gray-600 mb-3">Activity Summary</h4>
                      <div className="space-y-2">
                        <div className="flex justify-between">
                          <span className="text-sm text-gray-600">Active Projects</span>
                          <span className="text-sm font-medium text-gray-900">{selectedClient._count.projects}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-sm text-gray-600">Total Calls</span>
                          <span className="text-sm font-medium text-gray-900">{selectedClient._count.calls}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-sm text-gray-600">Last Contact</span>
                          <span className="text-sm font-medium text-gray-900">2 days ago</span>
                        </div>
                      </div>
                    </div>

                    <div className="pt-4 border-t border-gray-200 space-y-2">
                      <button className="w-full bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 flex items-center justify-center">
                        <Phone className="h-4 w-4 mr-2" />
                        Call Client
                      </button>
                      <button className="w-full bg-gray-100 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-200 flex items-center justify-center">
                        <Mail className="h-4 w-4 mr-2" />
                        Send Email
                      </button>
                      <button className="w-full bg-gray-100 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-200 flex items-center justify-center">
                        <Building className="h-4 w-4 mr-2" />
                        View Projects
                      </button>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center text-gray-500">
                  <Users className="h-12 w-12 text-gray-300 mx-auto mb-4" />
                  <p>Select a client to view details</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}