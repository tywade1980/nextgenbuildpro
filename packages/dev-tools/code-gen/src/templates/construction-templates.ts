/**
 * Construction Industry Templates
 * 
 * Specialized templates for construction industry projects including
 * Interior Finishes, Plumbing Systems, Electrical Systems, and Foundation work
 */

export interface ConstructionTask {
  id: string
  name: string
  description: string
  estimatedTime?: number // in hours
  dependencies?: string[]
  materials?: string[]
  tools?: string[]
  completed?: boolean
}

export interface ConstructionAssembly {
  id: string
  name: string
  description: string
  scope: string
  tasks: ConstructionTask[]
  estimatedCost?: number
  estimatedTime?: number // in hours
}

export interface ConstructionCategory {
  id: string
  name: string
  description: string
  trade: string
  scopes: {
    id: string
    name: string
    description: string
    assemblies: ConstructionAssembly[]
  }[]
}

export interface ConstructionTemplate {
  id: string
  name: string
  description: string
  category: 'interior-finishes' | 'plumbing' | 'electrical' | 'foundation'
  trade: string
  estimatedDuration: string
  complexity: 'basic' | 'intermediate' | 'advanced'
  categories: ConstructionCategory[]
  projectType: 'residential' | 'commercial' | 'industrial'
  tags: string[]
}

// Interior Trim & Finish Carpentry Template
export const interiorFinishesTemplate: ConstructionTemplate = {
  id: 'interior-finishes-enhanced',
  name: 'Interior Trim & Finish Carpentry (Enhanced)',
  description: 'Complete interior finishing including stairs, doors, windows, ceiling trim, and custom storage',
  category: 'interior-finishes',
  trade: 'Finish Carpentry',
  estimatedDuration: '4-6 weeks',
  complexity: 'advanced',
  projectType: 'residential',
  tags: ['carpentry', 'interior', 'finish-work', 'trim', 'custom-storage'],
  categories: [
    {
      id: 'stair-construction',
      name: 'Stair Construction',
      description: 'Complete stair installation including standard and curved staircases',
      trade: 'Finish Carpentry',
      scopes: [
        {
          id: 'standard-staircase',
          name: 'Standard Staircase',
          description: 'Traditional straight staircase construction',
          assemblies: [
            {
              id: 'standard-stair-assembly',
              name: 'Standard Staircase Assembly',
              description: 'Complete standard staircase installation',
              scope: 'Stair Construction',
              estimatedTime: 40,
              tasks: [
                {
                  id: 'layout-stair-dimensions',
                  name: 'Layout stair dimensions',
                  description: 'Measure and mark stair dimensions according to building codes'
                },
                {
                  id: 'cut-stringers',
                  name: 'Cut stringers',
                  description: 'Cut stair stringers to precise measurements'
                },
                {
                  id: 'install-stringers',
                  name: 'Install stringers',
                  description: 'Secure stringers to framing structure'
                },
                {
                  id: 'install-risers',
                  name: 'Install risers',
                  description: 'Install vertical riser boards'
                },
                {
                  id: 'install-treads',
                  name: 'Install treads',
                  description: 'Install horizontal tread boards'
                },
                {
                  id: 'install-skirt-boards',
                  name: 'Install skirt boards',
                  description: 'Install decorative skirt boards along walls'
                },
                {
                  id: 'install-handrail',
                  name: 'Install handrail',
                  description: 'Install safety handrail system'
                },
                {
                  id: 'install-balusters',
                  name: 'Install balusters',
                  description: 'Install vertical baluster supports'
                },
                {
                  id: 'install-newel-posts',
                  name: 'Install newel posts',
                  description: 'Install main support posts'
                },
                {
                  id: 'sand-finish-stairs',
                  name: 'Sand and finish stairs',
                  description: 'Final sanding and finishing of all stair components'
                }
              ]
            }
          ]
        },
        {
          id: 'curved-staircase',
          name: 'Curved Staircase',
          description: 'Advanced curved staircase construction',
          assemblies: [
            {
              id: 'curved-stair-assembly',
              name: 'Curved Staircase Assembly',
              description: 'Complete curved staircase installation',
              scope: 'Stair Construction',
              estimatedTime: 60,
              tasks: [
                {
                  id: 'create-curved-template',
                  name: 'Create curved stringer template',
                  description: 'Create precise template for curved stringer cuts'
                },
                {
                  id: 'cut-curved-stringers',
                  name: 'Cut curved stringers',
                  description: 'Cut curved stringers using template'
                },
                {
                  id: 'install-curved-stringers',
                  name: 'Install curved stringers',
                  description: 'Install curved stringers with proper support'
                },
                {
                  id: 'install-curved-risers',
                  name: 'Install curved risers',
                  description: 'Install curved riser boards'
                },
                {
                  id: 'install-curved-treads',
                  name: 'Install curved treads',
                  description: 'Install curved tread boards'
                },
                {
                  id: 'install-curved-handrail',
                  name: 'Install curved handrail',
                  description: 'Install curved handrail system'
                },
                {
                  id: 'install-curved-balusters',
                  name: 'Install balusters',
                  description: 'Install balusters for curved design'
                },
                {
                  id: 'install-curved-newel-posts',
                  name: 'Install newel posts',
                  description: 'Install newel posts for curved staircase'
                }
              ]
            }
          ]
        }
      ]
    },
    {
      id: 'door-installation',
      name: 'Door Installation',
      description: 'Interior door installation including standard and pocket doors',
      trade: 'Finish Carpentry',
      scopes: [
        {
          id: 'interior-door-single',
          name: 'Interior Door (Single)',
          description: 'Standard single interior door installation',
          assemblies: [
            {
              id: 'single-door-assembly',
              name: 'Interior Door (Single) Assembly',
              description: 'Complete single door installation',
              scope: 'Door Installation',
              estimatedTime: 8,
              tasks: [
                {
                  id: 'prepare-rough-opening',
                  name: 'Prepare rough opening',
                  description: 'Prepare and square the rough door opening'
                },
                {
                  id: 'install-door-jamb',
                  name: 'Install door jamb',
                  description: 'Install door jamb assembly'
                },
                {
                  id: 'square-plumb-jamb',
                  name: 'Square and plumb jamb',
                  description: 'Ensure jamb is square and plumb'
                },
                {
                  id: 'install-door-shims',
                  name: 'Install door shims',
                  description: 'Install shims for proper alignment'
                },
                {
                  id: 'secure-jamb-framing',
                  name: 'Secure jamb to framing',
                  description: 'Secure jamb to wall framing'
                },
                {
                  id: 'hang-door-slab',
                  name: 'Hang door slab',
                  description: 'Hang door slab in jamb'
                },
                {
                  id: 'install-hinges',
                  name: 'Install hinges (3 per door)',
                  description: 'Install three hinges per door'
                },
                {
                  id: 'install-door-stop',
                  name: 'Install door stop molding',
                  description: 'Install door stop molding'
                },
                {
                  id: 'mortise-strike-plate',
                  name: 'Mortise for strike plate',
                  description: 'Cut mortise for door strike plate'
                },
                {
                  id: 'install-lockset',
                  name: 'Install lockset',
                  description: 'Install door handle and locking mechanism'
                },
                {
                  id: 'install-door-casing',
                  name: 'Install door casing (both sides)',
                  description: 'Install decorative casing on both sides'
                },
                {
                  id: 'fill-nail-holes',
                  name: 'Fill nail holes',
                  description: 'Fill nail holes and prepare for finishing'
                }
              ]
            }
          ]
        },
        {
          id: 'pocket-door',
          name: 'Pocket Door',
          description: 'Space-saving pocket door installation',
          assemblies: [
            {
              id: 'pocket-door-assembly',
              name: 'Pocket Door Assembly',
              description: 'Complete pocket door installation',
              scope: 'Door Installation',
              estimatedTime: 12,
              tasks: [
                {
                  id: 'install-pocket-frame',
                  name: 'Install pocket door frame',
                  description: 'Install structural pocket door frame'
                },
                {
                  id: 'install-track-hardware',
                  name: 'Install track hardware',
                  description: 'Install track and roller hardware'
                },
                {
                  id: 'install-door-guides',
                  name: 'Install door guides',
                  description: 'Install door guide system'
                },
                {
                  id: 'hang-door-panel',
                  name: 'Hang door panel',
                  description: 'Hang door panel on track system'
                },
                {
                  id: 'install-pull-hardware',
                  name: 'Install pull hardware',
                  description: 'Install door pull hardware'
                },
                {
                  id: 'install-pocket-jamb',
                  name: 'Install door jamb',
                  description: 'Install pocket door jamb'
                },
                {
                  id: 'install-pocket-casing',
                  name: 'Install door casing',
                  description: 'Install pocket door casing'
                },
                {
                  id: 'adjust-door-operation',
                  name: 'Adjust door operation',
                  description: 'Fine-tune door operation and alignment'
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}

// Plumbing Systems Template
export const plumbingSystemsTemplate: ConstructionTemplate = {
  id: 'plumbing-systems-enhanced',
  name: 'Plumbing Systems (Enhanced)',
  description: 'Complete plumbing installation including rough and finish plumbing for bathrooms, kitchens, and utilities',
  category: 'plumbing',
  trade: 'Plumbing',
  estimatedDuration: '3-4 weeks',
  complexity: 'advanced',
  projectType: 'residential',
  tags: ['plumbing', 'water-systems', 'drainage', 'fixtures'],
  categories: [
    {
      id: 'rough-plumbing',
      name: 'Rough Plumbing',
      description: 'Installation of water supply and drainage systems within walls and floors',
      trade: 'Rough Plumbing',
      scopes: [
        {
          id: 'bathroom-rough-in',
          name: 'Bathroom Rough-in (Per Bathroom)',
          description: 'Complete rough plumbing for bathroom installations',
          assemblies: [
            {
              id: 'toilet-rough-in',
              name: 'Toilet Rough-in',
              description: 'Rough plumbing for toilet installation',
              scope: 'Bathroom Rough-in (Per Bathroom)',
              estimatedTime: 4,
              tasks: [
                {
                  id: 'install-waste-pipe',
                  name: 'Install 3" or 4" waste pipe',
                  description: 'Install main waste pipe connection'
                },
                {
                  id: 'install-toilet-flange',
                  name: 'Install toilet flange',
                  description: 'Install toilet mounting flange'
                },
                {
                  id: 'install-water-supply',
                  name: 'Install water supply line',
                  description: 'Install cold water supply line'
                },
                {
                  id: 'install-shutoff-rough',
                  name: 'Install shutoff valve rough-in',
                  description: 'Install shutoff valve rough-in'
                },
                {
                  id: 'pressure-test-water',
                  name: 'Pressure test water lines',
                  description: 'Test water lines for leaks'
                },
                {
                  id: 'test-drain-leaks',
                  name: 'Test drain for leaks',
                  description: 'Test drain connections for leaks'
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}

// Electrical Systems Template
export const electricalSystemsTemplate: ConstructionTemplate = {
  id: 'electrical-systems-enhanced',
  name: 'Electrical Systems (Enhanced)',
  description: 'Complete electrical installation including rough and finish electrical for all room types',
  category: 'electrical',
  trade: 'Electrical',
  estimatedDuration: '2-3 weeks',
  complexity: 'advanced',
  projectType: 'residential',
  tags: ['electrical', 'wiring', 'circuits', 'service-installation'],
  categories: [
    {
      id: 'rough-electrical',
      name: 'Rough Electrical',
      description: 'Installation of electrical wiring and boxes within walls',
      trade: 'Rough Electrical',
      scopes: [
        {
          id: 'room-wiring',
          name: 'Room Wiring (Per Room Type)',
          description: 'Electrical wiring for different room types',
          assemblies: [
            {
              id: 'bedroom-wiring',
              name: 'Bedroom Wiring (Standard)',
              description: 'Standard electrical wiring for bedrooms',
              scope: 'Room Wiring (Per Room Type)',
              estimatedTime: 8,
              tasks: [
                {
                  id: 'install-electrical-boxes',
                  name: 'Install electrical boxes (8 per room)',
                  description: 'Install electrical outlet and switch boxes'
                },
                {
                  id: 'run-lighting-wire',
                  name: 'Run 14/2 wire for lighting circuit (35 feet average)',
                  description: 'Install lighting circuit wiring'
                },
                {
                  id: 'run-receptacle-wire',
                  name: 'Run 14/2 wire for receptacle circuit (55 feet average)',
                  description: 'Install receptacle circuit wiring'
                },
                {
                  id: 'install-ceiling-fan-box',
                  name: 'Install ceiling fan box with brace',
                  description: 'Install reinforced ceiling fan box'
                },
                {
                  id: 'install-switch-boxes',
                  name: 'Install light switch boxes (2 per room)',
                  description: 'Install light switch electrical boxes'
                },
                {
                  id: 'install-receptacle-boxes',
                  name: 'Install receptacle boxes (6 per room)',
                  description: 'Install electrical receptacle boxes'
                },
                {
                  id: 'ground-all-boxes',
                  name: 'Ground all boxes',
                  description: 'Connect grounding wire to all electrical boxes'
                },
                {
                  id: 'label-circuits',
                  name: 'Label all circuits',
                  description: 'Label all circuits for identification'
                }
              ]
            }
          ]
        },
        {
          id: 'service-installation',
          name: 'Service Installation',
          description: 'Main electrical service and panel installation',
          assemblies: [
            {
              id: '200-amp-service',
              name: '200 Amp Service',
              description: 'Main electrical service installation',
              scope: 'Service Installation',
              estimatedTime: 12,
              tasks: [
                {
                  id: 'install-meter-base',
                  name: 'Install meter base',
                  description: 'Install electrical meter base'
                },
                {
                  id: 'install-service-cable',
                  name: 'Install service entrance cable',
                  description: 'Install main service entrance cable'
                },
                {
                  id: 'install-main-panel',
                  name: 'Install main panel',
                  description: 'Install main electrical panel'
                },
                {
                  id: 'install-main-breaker',
                  name: 'Install main breaker',
                  description: 'Install main circuit breaker'
                },
                {
                  id: 'install-grounding-rod',
                  name: 'Install grounding rod',
                  description: 'Install electrical grounding rod'
                },
                {
                  id: 'connect-grounding-wire',
                  name: 'Connect grounding wire',
                  description: 'Connect main grounding wire'
                },
                {
                  id: 'install-circuit-breakers',
                  name: 'Install circuit breakers',
                  description: 'Install individual circuit breakers'
                },
                {
                  id: 'connect-service-neutral',
                  name: 'Connect service neutral',
                  description: 'Connect service neutral wire'
                },
                {
                  id: 'label-panel-directory',
                  name: 'Label panel directory',
                  description: 'Create and install panel directory labels'
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}

// Foundation & Basement Template
export const foundationBasementTemplate: ConstructionTemplate = {
  id: 'foundation-basement-enhanced',
  name: 'Foundation & Basement (Enhanced)',
  description: 'Complete foundation and basement construction including concrete work and waterproofing',
  category: 'foundation',
  trade: 'Concrete',
  estimatedDuration: '6-8 weeks',
  complexity: 'advanced',
  projectType: 'residential',
  tags: ['foundation', 'concrete', 'basement', 'waterproofing'],
  categories: [
    {
      id: 'concrete',
      name: 'Concrete',
      description: 'Concrete foundation and basement construction',
      trade: 'Concrete',
      scopes: [
        {
          id: 'basement-construction',
          name: 'Basement Construction',
          description: 'Complete basement foundation construction',
          assemblies: [
            {
              id: 'full-basement-foundation',
              name: 'Full Basement Foundation',
              description: 'Complete basement foundation construction',
              scope: 'Basement Construction',
              estimatedTime: 120,
              tasks: [
                {
                  id: 'excavate-to-depth',
                  name: 'Excavate to required depth',
                  description: 'Excavate basement area to required depth'
                },
                {
                  id: 'install-footing-drains',
                  name: 'Install footing drains',
                  description: 'Install drainage system around footings'
                },
                {
                  id: 'form-pour-footings',
                  name: 'Form and pour footings',
                  description: 'Form and pour concrete footings'
                },
                {
                  id: 'form-basement-walls',
                  name: 'Form basement walls',
                  description: 'Set up forms for basement walls'
                },
                {
                  id: 'install-steel-reinforcement',
                  name: 'Install steel reinforcement',
                  description: 'Install rebar reinforcement in walls'
                },
                {
                  id: 'pour-concrete-walls',
                  name: 'Pour concrete walls',
                  description: 'Pour concrete for basement walls'
                },
                {
                  id: 'strip-forms',
                  name: 'Strip forms',
                  description: 'Remove concrete forms after curing'
                },
                {
                  id: 'apply-waterproofing',
                  name: 'Apply waterproofing membrane',
                  description: 'Apply waterproofing to exterior walls'
                },
                {
                  id: 'install-drainage-board',
                  name: 'Install drainage board',
                  description: 'Install drainage board against walls'
                },
                {
                  id: 'install-window-wells',
                  name: 'Install window wells',
                  description: 'Install basement window wells'
                },
                {
                  id: 'backfill-foundation',
                  name: 'Backfill foundation',
                  description: 'Backfill around foundation walls'
                }
              ]
            },
            {
              id: 'basement-floor',
              name: 'Basement Floor',
              description: 'Basement floor preparation and installation',
              scope: 'Basement Construction',
              estimatedTime: 24,
              tasks: [
                {
                  id: 'install-radon-system',
                  name: 'Install radon mitigation system',
                  description: 'Install radon mitigation piping system'
                },
                {
                  id: 'place-stone-base',
                  name: 'Place 4" stone base',
                  description: 'Place and level stone base material'
                },
                {
                  id: 'compact-stone-base',
                  name: 'Compact stone base',
                  description: 'Compact stone base to proper density'
                },
                {
                  id: 'install-vapor-barrier',
                  name: 'Install vapor barrier',
                  description: 'Install plastic vapor barrier over stone'
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}

export const constructionTemplates: ConstructionTemplate[] = [
  interiorFinishesTemplate,
  plumbingSystemsTemplate,
  electricalSystemsTemplate,
  foundationBasementTemplate
]

export function getConstructionTemplatesByCategory(category: string): ConstructionTemplate[] {
  return constructionTemplates.filter(template => template.category === category)
}

export function getConstructionTemplatesByTrade(trade: string): ConstructionTemplate[] {
  return constructionTemplates.filter(template => template.trade === trade)
}