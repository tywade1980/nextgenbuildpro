import { NextResponse } from 'next/server';
import { db } from '@/lib/db';
import { projects, clients, laborRates, buildingCodes, marketData, callLogs } from '@/lib/schema';
import { constructionMarketData, laborRatesData, buildingCodesData } from '@/data/construction-data';
import { AIModelManager } from '@/lib/ai-models';

export async function POST() {
  try {
    // Initialize AI Models
    const modelManager = AIModelManager.getInstance();
    await modelManager.initializeModels();

    // Seed construction market data
    for (const data of constructionMarketData) {
      await db.insert(marketData).values(data).onConflictDoNothing();
    }

    // Seed labor rates data
    for (const data of laborRatesData) {
      await db.insert(laborRates).values(data).onConflictDoNothing();
    }

    // Seed building codes data
    for (const data of buildingCodesData) {
      await db.insert(buildingCodes).values({
        codeNumber: data.codeNumber,
        title: data.title,
        description: data.description,
        category: data.category,
        jurisdiction: data.jurisdiction,
        effectiveDate: data.effectiveDate,
        requirements: JSON.stringify(data.requirements),
        penalties: JSON.stringify(data.penalties)
      }).onConflictDoNothing();
    }

    // Seed sample clients
    const sampleClients = [
      {
        name: 'Johnson Family',
        email: 'sarah.johnson@email.com',
        phone: '(555) 123-4567',
        addressLine1: '123 Maple Street',
        city: 'Atlanta',
        state: 'GA',
        zipCode: '30309',
        notes: 'Interested in kitchen renovation'
      },
      {
        name: 'Smith Construction LLC',
        email: 'contact@smithconstruction.com',
        phone: '(555) 987-6543',
        company: 'Smith Construction LLC',
        addressLine1: '456 Oak Avenue',
        city: 'Atlanta',
        state: 'GA',
        zipCode: '30318',
        notes: 'Commercial project partner'
      },
      {
        name: 'Davis Residence',
        email: 'mike.davis@email.com',
        phone: '(555) 456-7890',
        addressLine1: '789 Pine Road',
        city: 'Marietta',
        state: 'GA',
        zipCode: '30060',
        notes: 'Bathroom renovation project'
      }
    ];

    for (const client of sampleClients) {
      await db.insert(clients).values(client).onConflictDoNothing();
    }

    // Seed sample projects
    const clientRecords = await db.select().from(clients).limit(3);
    const sampleProjects = [
      {
        name: 'Johnson Kitchen Renovation',
        description: 'Complete kitchen remodel including cabinets, countertops, and appliances',
        type: 'residential',
        status: 'active',
        startDate: '2024-02-01',
        endDate: '2024-03-15',
        estimatedCost: 35000,
        clientId: clientRecords[0]?.id,
        addressLine1: '123 Maple Street',
        city: 'Atlanta',
        state: 'GA',
        zipCode: '30309',
        buildingCodes: JSON.stringify(['IRC-2021-R302', 'IRC-2021-E3501']),
        permits: JSON.stringify(['Kitchen Renovation Permit'])
      },
      {
        name: 'Smith Office Building',
        description: 'New 3-story office building construction',
        type: 'commercial',
        status: 'planning',
        startDate: '2024-03-01',
        endDate: '2024-12-31',
        estimatedCost: 750000,
        clientId: clientRecords[1]?.id,
        addressLine1: '456 Oak Avenue',
        city: 'Atlanta',
        state: 'GA',
        zipCode: '30318',
        buildingCodes: JSON.stringify(['ADA-2010-206', 'IECC-2021-C405']),
        permits: JSON.stringify(['Building Permit', 'Electrical Permit', 'Plumbing Permit'])
      },
      {
        name: 'Davis Bathroom Renovation',
        description: 'Master bathroom renovation with accessibility features',
        type: 'residential',
        status: 'completed',
        startDate: '2024-01-15',
        endDate: '2024-02-28',
        estimatedCost: 18000,
        actualCost: 19500,
        clientId: clientRecords[2]?.id,
        addressLine1: '789 Pine Road',
        city: 'Marietta',
        state: 'GA',
        zipCode: '30060',
        buildingCodes: JSON.stringify(['IRC-2021-P2801', 'ADA-2010-206']),
        permits: JSON.stringify(['Plumbing Permit'])
      }
    ];

    for (const project of sampleProjects) {
      await db.insert(projects).values(project).onConflictDoNothing();
    }

    // Seed sample call logs
    const sampleCallLogs = [
      {
        phoneNumber: '(555) 111-2222',
        callerName: 'Jennifer Williams',
        callType: 'incoming',
        status: 'answered',
        duration: 320,
        transcript: 'Hello, I\'m interested in getting a quote for a deck addition to my home.',
        sentiment: 'positive',
        priority: 'medium',
        aiScreeningResult: JSON.stringify({
          intent: 'project_inquiry',
          urgency: 'medium',
          category: 'new_business',
          confidence: 0.88,
          suggestedAction: 'Schedule estimate appointment',
          keyTopics: ['deck addition', 'home improvement', 'quote request']
        }),
        followUpRequired: 1
      },
      {
        phoneNumber: '(555) 333-4444',
        callerName: 'Robert Chen',
        callType: 'incoming',
        status: 'screened',
        duration: 45,
        transcript: 'I need emergency repair for a broken water pipe in my basement.',
        sentiment: 'negative',
        priority: 'urgent',
        aiScreeningResult: JSON.stringify({
          intent: 'emergency_repair',
          urgency: 'urgent',
          category: 'emergency',
          confidence: 0.95,
          suggestedAction: 'Immediate dispatch required',
          keyTopics: ['emergency', 'water pipe', 'basement', 'repair']
        }),
        followUpRequired: 1
      }
    ];

    for (const callLog of sampleCallLogs) {
      await db.insert(callLogs).values(callLog).onConflictDoNothing();
    }

    return NextResponse.json({
      success: true,
      message: 'Database initialized and seeded successfully',
      data: {
        clients: clientRecords.length,
        projects: sampleProjects.length,
        laborRates: laborRatesData.length,
        buildingCodes: buildingCodesData.length,
        marketData: constructionMarketData.length,
        callLogs: sampleCallLogs.length
      }
    });

  } catch (error) {
    console.error('Error initializing database:', error);
    return NextResponse.json({
      success: false,
      error: 'Failed to initialize database',
      details: error instanceof Error ? error.message : 'Unknown error'
    }, { status: 500 });
  }
}