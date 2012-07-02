import csv
import sys
from itertools import izip
'''
tt-combine.py takes the results from multiple travel time calculations and
combines them by keeping the minimum travel time for each O-D pair.

Usage:
tt-combine.py results1.csv results2.csv ...
'''

def same_pairs(rows):
    otaz = rows[0][0]
    dtaz = rows[0][1]
    for row in rows:
        if row[0] != otaz or row[1] != dtaz:
            return False
    return True

def min_tt(rows):
    times = (float(row[2]) for row in rows)
    return min(times)

if __name__ == '__main__':
    infiles = sys.argv[1:]
    readers = []
    for infile in infiles:
        readers.append(csv.reader(open(infile, 'rb')))
    writer = csv.writer(sys.stdout, quoting=csv.QUOTE_NONNUMERIC)
    writer.writerow(["otaz", "dtaz", "mins"])

    for rows in izip(*readers):
        if not same_pairs(rows):
            raise ValueError('Different pairs')
        otaz = str(int(float(rows[0][0])))
        dtaz = str(int(float(rows[0][1])))
        min_time = min_tt(rows)
        writer.writerow([otaz, dtaz, min_time])
