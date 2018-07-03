import os
from glob import glob
from shutil import copyfile

propertiesFile = 'queryExpansion.properties'
method = 'TermFrequency'

contentValues = list(range(0,10+1))
genreValues = list(range(0,10+1))

# TODO inverter genreK - contentK
def updateProperties (genreK, contentK): # compose queryExpansion.properties file
    numSeeds = 15
    with open(propertiesFile, 'w') as f:
        f.write('use:true\n')
        f.write('method:{}\n'.format(method))
        f.write('numSeeds:{}\n'.format(numSeeds))
        f.write('contentK:{}\n'.format(contentK))
        f.write('genreK:{}\n'.format(genreK))
    f.close()

for i in contentValues:
    for j in genreValues:
        updateProperties(i, j) # update queryExpansion.properties
        os.system('java -jar target/focused-crawler-seq-1.0-SNAPSHOT-jar-with-dependencies.jar') # run collector
        os.makedirs('experimentos/content{}/genre{}'.format(i,j)) # create directory
        os.rename('crawledPages', 'experimentos/content{}/genre{}/crawledPages'.format(i,j)) # save crawledPages
        os.rename('relevantPages', 'experimentos/content{}/genre{}/relevantPages'.format(i,j)) # save relevantPages
        copyfile('content.expansion', 'experimentos/content{}/genre{}/content.expansion'.format(i,j)) # save content.expansion
        copyfile('genre.expansion', 'experimentos/content{}/genre{}/genre.expansion'.format(i,j)) # save genre.expansion
        copyfile('queryExpansion.properties', 'experimentos/content{}/genre{}/queryExpansion.properties'.format(i,j)) # save queryExpansion.properties

print('experiments finished')
