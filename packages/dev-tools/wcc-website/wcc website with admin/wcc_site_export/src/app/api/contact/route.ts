import { NextResponse } from 'next/server';
import nodemailer from 'nodemailer';

// Define the expected request body structure
interface ContactFormData {
  name: string;
  email: string;
  phone: string;
  projectType: string;
  message: string;
}

// In-memory storage for leads (in a production app, this would be a database)
let leads: any[] = [];

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

    // Set up email transporter
    const transporter = nodemailer.createTransport({
      host: process.env.EMAIL_HOST || 'smtp.gmail.com',
      port: parseInt(process.env.EMAIL_PORT || '587'),
      secure: process.env.EMAIL_SECURE === 'true',
      auth: {
        user: process.env.EMAIL_USER || 'your-email@gmail.com',
        pass: process.env.EMAIL_PASSWORD || 'your-password'
      }
    });

    // Email content
    const mailOptions = {
      from: process.env.EMAIL_FROM || 'website@wadecustomcarpentry.com',
      to: 'tyler@dublinremodelingservices.net',
      subject: `New Website Inquiry: ${data.projectType || 'General Inquiry'}`,
      text: `
        New inquiry from your website:
        
        Name: ${data.name}
        Email: ${data.email}
        Phone: ${data.phone || 'Not provided'}
        Project Type: ${data.projectType || 'Not specified'}
        
        Message:
        ${data.message}
        
        This lead has been added to your admin dashboard.
      `,
      html: `
        <h2>New inquiry from your website</h2>
        <p><strong>Name:</strong> ${data.name}</p>
        <p><strong>Email:</strong> ${data.email}</p>
        <p><strong>Phone:</strong> ${data.phone || 'Not provided'}</p>
        <p><strong>Project Type:</strong> ${data.projectType || 'Not specified'}</p>
        <h3>Message:</h3>
        <p>${data.message.replace(/\n/g, '<br>')}</p>
        <p>This lead has been added to your admin dashboard.</p>
      `
    };

    // Send the email
    try {
      await transporter.sendMail(mailOptions);
    } catch (error) {
      console.error('Error sending email:', error);
      // Continue execution even if email fails - we still want to save the lead
    }

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
