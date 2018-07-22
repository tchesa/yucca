import webbrowser
import os
from glob import glob
import sys

contentK = 0
genreK = 0
if (len(sys.argv) == 3):
    contentK = int(sys.argv[1])
    genreK = int(sys.argv[2])
else:
    print("missing arguments")
    sys.exit(1)

gabarito = 'gabarito.txt'
relevantLocation = 'content{}/genre{}/relevantPages'.format(contentK, genreK)
crawledLocation = 'content{}/genre{}/crawledPages'.format(contentK, genreK)

gabaritoUrls = []
gabaritoValues = []

print("lendo gabarito...")
with open(gabarito, 'r') as f:
    for line in f:
        pieces = line.split(',')
        url = ",".join(pieces[:len(pieces)-1])
#        url = line.split(',')[0]
        value = int(line.split(',')[len(pieces)-1])
        gabaritoUrls.append(url)
        gabaritoValues.append(value)
f.close()

relevantUrls, relevantValues = [], []
nonRelevantUrls, nonRelevantValues = [], []

print("lendo paginas...")
pages = glob(relevantLocation + "/*.collected")
for page in pages:
    with open(page, 'r') as f:
        for line in f:
            if (line[:4] == 'url='):
                url = line.replace('url=','').strip()
                relevantUrls.append(url)
                break
        f.close()

pages = glob(crawledLocation + "/*.collected")
for page in pages:
    with open(page, 'r') as f:
        for line in f:
            if (line[:4] == 'url='):
                url = line.replace('url=','').strip()
                if (url in relevantUrls): # the page is relevant
                    break
                nonRelevantUrls.append(url)
                break
        f.close()

relevantValues = [-1 for x in range(0,len(relevantUrls))]
nonRelevantValues = [-1 for x in range(0,len(nonRelevantUrls))]
tp, fp, tn, fn = 0, 0, 0, 0
stop = False

print("calculando...")
for i,url in enumerate(relevantUrls):
    if (stop):
        break
    if (url in gabaritoUrls):
        index = gabaritoUrls.index(url)
        relevantValues[i] = gabaritoValues[index]
    else:
        print ('({} of {}) {}'.format(i+1, len(relevantUrls) + len(nonRelevantUrls), url))
        webbrowser.open(url, autoraise=False)
        res = ''
        while (res != 'y' and res != 'n' and res != 'k'):
            res = input('relevant? [Y/n] ').lower()
            if (res != 'y' and res != 'n' and res != 'k'):
                print('invalid answer...')
            elif (res == 'k'):
                stop = True
            elif (res == 'y'):
                relevantValues[i] = 1
                gabaritoUrls.append(url)
                gabaritoValues.append(relevantValues[i])
            else:
                relevantValues[i] = 0
                gabaritoUrls.append(url)
                gabaritoValues.append(relevantValues[i])
    if (stop):
        break
    elif (relevantValues[i] == 1):
        tp += 1
    elif (relevantValues[i] == 0):
        fp += 1

for i,url in enumerate(nonRelevantUrls):
    if (stop):
        break
    if (url in gabaritoUrls):
        index = gabaritoUrls.index(url)
        nonRelevantValues[i] = gabaritoValues[index]
    else:
        print ('({} of {}) {}'.format(len(relevantUrls)+i+1, len(relevantUrls) + len(nonRelevantUrls), url))
        webbrowser.open(url, autoraise=False)
        res = ''
        while (res != 'y' and res != 'n' and res != 'k'):
            res = input('relevant? [Y/n] ').lower()
            if (res != 'y' and res != 'n' and res != 'k'):
                print('invalid answer...')
                continue
            elif (res == 'k'):
                stop = True
            elif (res == 'y'):
                nonRelevantValues[i] = 1
                gabaritoUrls.append(url)
                gabaritoValues.append(nonRelevantValues[i])
            else:
                nonRelevantValues[i] = 0
                gabaritoUrls.append(url)
                gabaritoValues.append(nonRelevantValues[i])
    if (stop):
        break
    elif (nonRelevantValues[i] == 1):
        fn += 1
    elif (nonRelevantValues[i] == 0):
        tn += 1

print("tp={}, fp={}, tn={}, fn={}".format(tp, fp, tn, fn))

precision = tp/(tp+fp)
recall = tp/(tp+fn)
f1 = 2*((precision*recall)/(precision+recall))
print("precision={}, recall={}, F1={}".format(precision, recall, f1))

print("atualizando gabarito...")
with open(gabarito, 'w') as f:
    for i in range(0, len(gabaritoUrls)):
        f.write('{},{}\n'.format(gabaritoUrls[i], str(gabaritoValues[i])))
    f.close()
