NEXUS Transit Travel Time Calculator
====================================

This program takes a database of transit schedules and returns a TAZ-to-TAZ travel time matrix. It was initially developed in 2011 as part of the Access to Destinations project for the Minnesota Department of Transportation. It is compatible with the General Transit Feed Specification (GTFS) format and can be used with any GTFS schedule, as long as a few other inputs are provided.

The NEXUS Transit Travel Time Calculator implements a variation of the [RAPTOR algorithm][raptor] for efficient route-based transit routing.

Inputs
------
Three input files are needed. The assumed format is comma delimited with a one line header. File names can be set within the program.

*	GTFS file

	Any standard GTFS file.

*	Points file

    A CSV file identifying geographic points from and to which travel times should be calculated. An example is included. It must have the following fields, in this order:
    
    *   `GEOID`: A string uniquely identifying each point. In the example, these are US Census block IDs.
    
    *   `REGION`: A string identifying which summary region the point belongs to. This must match an entry in the regions file (below). In the example, these are Transportation Analysis Zones (TAZs) as defined by the Metropolitan Council.
    
    *   `LAT`: A floating-point number identifying the latitude of the point, in the WGS84 coordinate system.
    
    *   `LON`: A floating-point number identifying the longitude of the point, in the WGS84 coordinate system.

*   Region file

    A CSV file identifying summary regions at which the results of the travel time calculation should be reported. And example is included. Travel times are calculated between each pair of points identified in the points file, and then averaged to regions. The regions file must have the following fields, in this order:
    
    *   `ID`: A string uniquely identifying each regions. In the example, these are Transportation Analysis Zones (TAZs) as defined by the Metropolitan Council.
    
    *   `LAT`: A floating-point number identifying the latitude of the region’s centroid, in the WGS84 coordinate system.
    
    *   `LON`: A floating-point number identifying the longitude of the region’s centroid, in the WGS84 coordinate system.

Usage
-----

The program is built as an executable, command-line-driven JAR file. An example invocation is:

```
java -Xmx4G -jar NexusTTTCalc -g gtfs_file.zip -p points.csv -r regions.csv -id SEP09-Multi-Weekday-01 -s 25200 -e 32400 -w 900 -b 2 -o results.csv
```

This implementation is memory-intensive; the -Xmx option is used to significantly increase the amount of memory available in the JVM. The exact amount of memory required will depend on the contents of the GTFS, points, and regions files used.

The program is controlled by several options:

*   `-g [GTFS file]`
   
    Read schedule information from the specified GTFS file

*   `-id [service id]`

    The service ID to process. This must match a service ID in the calendar.txt file of the specified GTFS file. Only one service ID can be processed at a time.

*   `-p [points file]`
    
    Read point information from the specified file

*   `-r [regions file]`
    
    Read region information from the specified file

*   `-o [output file]`
    
    Write the output matrix to the specified file

*   `-s #`

    Start time of day, in seconds past midnight (e.g. 7AM = 25200). Trips which depart before this time will not be included in travel time calculations.
    
*   `-e #`

    End time of day, in seconds past midnight (e.g. 9AM = 32400). Trips which depart after this time will not be included in travel time calculations.

*   `-b #` (optional)

    The maximum number of boardings. 0 will allow walking trips only; 1 will allow a single boarding and no transfers; 2 will allow a single transfer; etc. The default is 2.

*   `-w #` (optional)

    The maximum allowable wait time for transfers, in seconds. Potential transfer trips which depart later than the current time plus this value will be ignored. The default is 900 seconds (15 minutes).
    
*   `-mp #` (optional)

    The maximum number of concurrent threads. Transit routing is [embarrassingly parallel][parallel] and computation times can be significantly decreased by allowing multithreading. The default is 1 (single-threaded operation).

Development
-----------
The NEXUS Transit Travel Time Calculator uses Maven for build management. To build from source, it should be sufficient to run

```
mvn package
```

in the project root directory.

License and Copyright
---------------------
The NEXUS Transit Travel Time Calculator is released under the GNU General Public License version 3.0. See the LICENSE and http://www.gnu.org/licenses/gpl.html for more information.

Copyright 2011, 2012 University of Minnesota

[raptor]: http://research.microsoft.com/apps/pubs/default.aspx?id=156567
[parallel]: http://en.wikipedia.org/wiki/Embarrassingly_parallel