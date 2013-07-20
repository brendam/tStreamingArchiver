import os
import codecs
import json
import time
import csv
# NOTE: Excel on mac doesn't work for multibyte unicode characters like japanese,
# so they will not be displayed properly. OpenOffice or Google Spreadsheets work.
start_time = time.time()

keyword = u'smile' # from example searches.txt

record_seperator = "==================~===================\n"

deleted_tweet_ids = []
# this should be an up to date deletion notice file
with open('sample_deletion.csv', 'rb') as csvfile:
    csvreader = csv.reader(csvfile, delimiter=',')
    for row in csvreader:
        if row[0] != 'id': deleted_tweet_ids.append(row[0])

files = []
for dirname, dirnames, filenames in os.walk('data/2013/'):
    for filename in filenames:
        if not filename.startswith('.'):
            files.append(os.path.join(dirname, filename))

files.sort()

first = True
with open('smile_data.csv', 'wb') as csvfile:
    # csvfile.write((u'\uFEFF\n').encode('utf-8'))
    csvwriter = csv.writer(csvfile, dialect=csv.excel)
    csvwriter.writerow(['id', 'id_str', 'text', 'user_id', 'user_id_str', 'user_name', 'user_screen_name'])

    for file in files:
        f = codecs.open(file, encoding='utf-8', mode='r')
        data = f.read()
        f.close()
        for text in data.split(record_seperator):
            if text.startswith('StatusDeletionNoticeImpl{') or text.startswith('TrackLimitationNotice') or len(text) == 0:
                None # ignore deletion notice or track limitation notice and empty record (at least 1 at end of file)
            else:
                try:
                    # load tweet as dict from json object
                    tweet = json.loads(text)
                except ValueError, e:
                    print 'Error: ', e, text
                if first:
                    print '\nAvailable tweet keys:', tweet.keys()
                    print "\nAvailable tweet['user'] keys:", tweet['user'].keys(), '\n'
                    first = False
                if tweet['id'] in deleted_tweet_ids:
                    print "skipped deleted tweet", tweet['id']
                else:
                    if keyword in tweet['text']:
                        # having the twitter id as a number causes problems with excel using E notation and truncating the significant digits, so save str one as alternate
                        csvwriter.writerow([tweet['id'], '\''+tweet['id_str'], tweet['text'].encode('utf-8'), tweet['user']['id'], '\''+tweet['user']['id_str'], tweet['user']['name'].encode('utf-8'), tweet['user']['screen_name'].encode('utf-8')])

end_time = time.time()
print "Elapsed time: " + str(end_time - start_time) + " seconds"
