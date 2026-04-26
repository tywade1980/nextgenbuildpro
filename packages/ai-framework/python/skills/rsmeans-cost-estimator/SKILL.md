---
name: rsmeans-cost-estimator
description: "Industry-standard construction cost estimation using RSMeans data. Use for: granular job costing, labor hour projections, material unit pricing, and applying local city cost indexes (CCI)."
---

# RSMeans Cost Estimator Skill

This skill provides Manus with the ability to perform highly accurate construction cost estimation based on RSMeans standards, specifically tailored for design-build remodeling.

## Core Workflows

### 1. Granular Job Costing
When asked to estimate costs for a specific task or project:
1. **Identify CSI Divisions**: Map the scope to relevant CSI MasterFormat divisions (e.g., Division 06 for Carpentry).
2. **Retrieve Unit Costs**: Fetch industry-standard costs for materials and labor hours per unit.
3. **Apply Local Adjustments**: Use the City Cost Index (CCI) for the project location (e.g., Columbus, OH) to adjust national averages.
4. **Calculate Labor Times**: Provide "Projected Labor Times" based on man-hour data, adjusted for project complexity.

### 2. Labor & Performance Benchmarking
- Compare real-world performance data against RSMeans benchmarks to identify efficiency gains or losses.
- Use RSMeans "Crew" data to determine the most efficient staffing for specific tasks.

## Key Resources
- **Cost Data Structure**: See `references/cost_data_structure.md` for information on CSI divisions and unit cost components.

## Best Practices
- **Be Granular**: Never provide a "lump sum" without breaking it down into Material, Labor, and Equipment components.
- **Verify Sources**: When RSMeans data is used, explicitly state that the estimate is based on industry standards.
- **Complexity Multipliers**: For high-end custom work (Wade Custom Carpentry standard), always add a 15-30% labor multiplier to standard RSMeans man-hours to account for the extra care and precision required.
