# Swiss Ephemeris Data Files

This directory should contain JPL ephemeris files for maximum precision calculations.

## Required Files (Optional but Recommended)

For highest precision, place the following JPL ephemeris files here:
- `sepl_18.se1` - Planetary positions
- `semo_18.se1` - Moon positions
- `seas_18.se1` - Asteroid data

## Download

These files can be downloaded from:
- https://www.astro.com/ftp/swisseph/ephe/

## Note

If these files are not present, Swiss Ephemeris will use the built-in Moshier ephemeris, which is still highly accurate but slightly less precise than JPL data.

The app will automatically copy any bundled ephemeris files from assets to the app's files directory on first run.
