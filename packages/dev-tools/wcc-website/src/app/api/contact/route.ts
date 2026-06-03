import { NextResponse } from 'next/server';
import nodemailer from 'nodemailer';

// Force dynamic for API routes
export const dynamic = 'force-dynamic';

// Define the expected request body structure
interface ContactFormData {
  name: string;
  email: string;
  phone: string;
  projectType: string;
  message: string;
}

// In-memory storage for leads (in a production app, this would be a database)
const leads: ContactFormData[] = [];

export async function POST(request: Request) {
  try {
    // Parse the request body
    const data: ContactFormData = await request.json();
    
    // Basic validation
    if (!data.name || !data.email || !data.message) {
      return NextResponse.json(
        { error: 'Name, email, and message are required fields' },
        { status: 400 }
      );
    }

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(data.email)) {
      return NextResponse.json(
        { error: 'Please provide a valid email address' },
        { status: 400 }
      );
    }

    // Create a new lead object
    const newLead = {
      id: `lead-${Date.now()}`,
      name: data.name,
      email: data.email,
      phone: data.phone || 'Not provided',
      projectType: data.projectType || 'Not specified',
      message: data.message,
      status: 'new',
      dateReceived: new Date().toISOString(),
      notes: ''
    };

    // Store the lead (in a real app, this would save to a database)
    leads.push(newLead);

    // Return success response
    return NextResponse.json({ 
      success: true, 
      message: 'Your message has been sent successfully!',
      leadId: newLead.id
    });
    
  } catch (error) {
    console.error('Contact form error:', error);
    return NextResponse.json(
      { error: 'An error occurred while processing your request' },
      { status: 500 }
    );
  }
}

// API endpoint to get all leads (would be protected in a real app)
export async function GET() {
  return NextResponse.json({ leads });
}
