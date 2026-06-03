# RSMeans Cost Data Structure

## Core Data Components
- **CSI MasterFormat**: All data is organized by the Construction Specifications Institute (CSI) 50-division MasterFormat.
- **Unit Costs**:
  - **Material**: Cost per unit (LF, SF, EA, etc.).
  - **Labor**: Man-hours per unit and hourly rate.
  - **Equipment**: Rental or operational cost per unit.
- **Total In-Place Cost**: Sum of Material + Labor + Equipment.

## Common Remodeling Divisions
| Division | Description | Common Tasks |
| :--- | :--- | :--- |
| **02** | Existing Conditions | Selective Demolition, Site Prep |
| **06** | Wood, Plastics, Composites | Rough Carpentry, Finish Carpentry, Cabinetry |
| **09** | Finishes | Plaster/Drywall, Tiling, Painting |
| **22** | Plumbing | Fixtures, Piping, Drainage |

## Adjustment Factors
1. **City Cost Index (CCI)**: Multiply national average by the local CCI (e.g., Columbus, OH).
2. **Project Size**: Small projects (remodels) often carry a 10-25% "small project" premium.
3. **Complexity**: Custom design-build work requires adding "Complexity Multipliers" to standard labor times.
