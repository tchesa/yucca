import os
from glob import glob
from shutil import copyfile

propertiesFile = 'queryExpansion.properties'
method = 'TermFrequency'

contentValues = [0, 0, 0, 0, 0]
genreValues = [0, 0, 0, 0, 0]

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

n = 0
for i in contentValues:
    for j in genreValues:
        updateProperties(i, j) # update queryExpansion.properties
        os.system('java -jar target/focused-crawler-seq-1.0-SNAPSHOT-jar-with-dependencies.jar') # run collector
        # os.makedirs('experimentos0/content{}/genre{}'.format(i,j)) # create directory
        # os.rename('crawledPages', 'experimentos0/content{}/genre{}/crawledPages'.format(i,j)) # save crawledPages
        # os.rename('relevantPages', 'experimentos0/content{}/genre{}/relevantPages'.format(i,j)) # save relevantPages
        # copyfile('content.expansion', 'experimentos0/content{}/genre{}/content.expansion'.format(i,j)) # save content.expansion
        # copyfile('genre.expansion', 'experimentos0/content{}/genre{}/genre.expansion'.format(i,j)) # save genre.expansion
        # copyfile('queryExpansion.properties', 'experimentos0/content{}/genre{}/queryExpansion.properties'.format(i,j)) # save
        os.makedirs('experimentos0/experiment{}'.format(n)) # create directory
        os.rename('crawledPages', 'experimentos0/experiment{}/crawledPages'.format(n)) # save crawledPages
        os.rename('relevantPages', 'experimentos0/experiment{}/relevantPages'.format(n)) # save relevantPages
        copyfile('content.expansion', 'experimentos0/experiment{}/content.expansion'.format(n)) # save content.expansion
        copyfile('genre.expansion', 'experimentos0/experiment{}/genre.expansion'.format(n)) # save genre.expansion
        copyfile('queryExpansion.properties', 'experimentos0/experiment{}/queryExpansion.properties'.format(n)) # save queryExpansion.properties
        n += 1

print('experiments finished')
