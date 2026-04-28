import { PrismaClient } from '@prisma/client'

const prisma = new PrismaClient()

async function main() {
  console.log('🌱 Seeding database...')

  // Create sample clients
  const client1 = await prisma.client.create({
    data: {
      name: 'ABC Construction Corp',
      email: 'contact@abcconstruction.com',
      phone: '+1-555-0123',
      address: '123 Builder St, Construction City, CC 12345',
    },
  })

  const client2 = await prisma.client.create({
    data: {
      name: 'XYZ Development LLC',
      email: 'info@xyzdevelopment.com',
      phone: '+1-555-0456',
      address: '456 Developer Ave, Building Town, BT 67890',
    },
  })

  // Create sample projects
  const project1 = await prisma.project.create({
    data: {
      name: 'Residential Complex A',
      description: 'Construction of a 50-unit residential complex',
      startDate: new Date('2024-01-15'),
      endDate: new Date('2024-12-31'),
      status: 'active',
      budget: 2500000,
      location: '789 Construction Blvd, Build City, BC 11111',
      clientId: client1.id,
    },
  })

  const project2 = await prisma.project.create({
    data: {
      name: 'Office Building Renovation',
      description: 'Complete renovation of a 10-story office building',
      startDate: new Date('2024-03-01'),
      endDate: new Date('2024-08-30'),
      status: 'planning',
      budget: 1200000,
      location: '321 Office Plaza, Business District, BD 22222',
      clientId: client2.id,
    },
  })

  // Create sample tasks
  await prisma.task.create({
    data: {
      title: 'Foundation Work',
      description: 'Pour concrete foundation for all buildings',
      status: 'completed',
      priority: 'high',
      startDate: new Date('2024-01-15'),
      endDate: new Date('2024-02-28'),
      projectId: project1.id,
    },
  })

  await prisma.task.create({
    data: {
      title: 'Structural Framing',
      description: 'Install steel and wooden framework',
      status: 'in_progress',
      priority: 'high',
      startDate: new Date('2024-03-01'),
      projectId: project1.id,
    },
  })

  // Create sample labor rates
  await prisma.laborRate.create({
    data: {
      role: 'Senior Carpenter',
      hourlyRate: 45.00,
      projectId: project1.id,
    },
  })

  await prisma.laborRate.create({
    data: {
      role: 'Electrician',
      hourlyRate: 55.00,
      projectId: project1.id,
    },
  })

  await prisma.laborRate.create({
    data: {
      role: 'Plumber',
      hourlyRate: 50.00,
      projectId: project2.id,
    },
  })

  // Create sample materials
  await prisma.material.create({
    data: {
      name: 'Concrete (Ready Mix)',
      quantity: 500,
      unit: 'cubic yards',
      cost: 25000,
      supplier: 'QuickCrete Suppliers',
      projectId: project1.id,
    },
  })

  await prisma.material.create({
    data: {
      name: 'Steel Rebar',
      quantity: 2000,
      unit: 'linear feet',
      cost: 15000,
      supplier: 'Metro Steel Supply',
      projectId: project1.id,
    },
  })

  // Create sample building codes
  await prisma.buildingCode.create({
    data: {
      code: 'IBC-2021-Ch7',
      description: 'Fire and smoke protection requirements for high-rise buildings',
      category: 'fire_safety',
      jurisdiction: 'International Building Code',
      effectiveDate: new Date('2021-01-01'),
    },
  })

  await prisma.buildingCode.create({
    data: {
      code: 'NEC-2020-Art250',
      description: 'Grounding and bonding requirements for electrical systems',
      category: 'electrical',
      jurisdiction: 'National Electrical Code',
      effectiveDate: new Date('2020-01-01'),
    },
  })

  // Create sample market data
  await prisma.marketData.create({
    data: {
      region: 'Pacific Northwest',
      category: 'labor',
      metric: 'average_hourly_rate',
      value: 47.50,
      unit: 'USD/hour',
      date: new Date('2024-01-01'),
      source: 'Bureau of Labor Statistics',
    },
  })

  await prisma.marketData.create({
    data: {
      region: 'Southwest',
      category: 'materials',
      metric: 'concrete_price_index',
      value: 125.8,
      unit: 'index',
      date: new Date('2024-01-01'),
      source: 'Construction Materials Index',
    },
  })

  // Create sample AI models
  await prisma.aIModel.create({
    data: {
      name: 'GPT-4 Turbo',
      type: 'llm',
      provider: 'openai',
      modelId: 'gpt-4-turbo-preview',
      isActive: true,
      configuration: {
        temperature: 0.7,
        max_tokens: 2000,
        top_p: 1,
      },
    },
  })

  await prisma.aIModel.create({
    data: {
      name: 'Claude 3 Haiku',
      type: 'llm', 
      provider: 'anthropic',
      modelId: 'claude-3-haiku-20240307',
      isActive: true,
      configuration: {
        temperature: 0.5,
        max_tokens: 1500,
      },
    },
  })

  await prisma.aIModel.create({
    data: {
      name: 'Whisper',
      type: 'speech',
      provider: 'openai',
      modelId: 'whisper-1',
      isActive: true,
      configuration: {
        language: 'en',
      },
    },
  })

  // Create sample call logs
  await prisma.call.create({
    data: {
      phoneNumber: '+1-555-0123',
      clientId: client1.id,
      status: 'answered',
      duration: 180,
      transcript: 'Client called regarding the residential complex project timeline. Discussed current progress and upcoming milestones.',
      summary: 'Project status update call - client satisfied with progress',
      sentiment: 'positive',
      priority: 'normal',
    },
  })

  await prisma.call.create({
    data: {
      phoneNumber: '+1-555-9999',
      status: 'screened',
      duration: 30,
      transcript: 'Automated sales call detected. Caller was asking about general construction services.',
      summary: 'Sales call - not a priority',
      sentiment: 'neutral',
      priority: 'low',
    },
  })

  console.log('✅ Database seeded successfully!')
}

main()
  .catch((e) => {
    console.error('❌ Error seeding database:', e)
    process.exit(1)
  })
  .finally(async () => {
    await prisma.$disconnect()
  })