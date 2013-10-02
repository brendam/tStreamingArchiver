import os
import codecs
import json
import time
import csv
# NOTE: Excel on mac doesn't work for multibyte unicode characters like japanese,
# so they will not be displayed properly. OpenOffice or Google Spreadsheets work.
start_time = time.time()

# set keyword and start and end date if needed
keyword =  u'smile' # from example searches.txt
start_month = 0 # use this to process all data
# start_month = 8 # August
start_day = 3   # 3rd
end_month = 9
end_day = 8


record_seperator = "==================~===================\n"

def utf8_encode(item):
    new_item = ''
    if item != None:
        new_item = item.encode('utf8')
    return new_item

deleted_tweet_ids = []
# this should be an up to date deletion notice file
with open('sample_deletion.csv', 'rb') as csvfile:
    csvreader = csv.reader(csvfile, delimiter=',')
    for row in csvreader:
        if row[0] != 'id': deleted_tweet_ids.append(row[0])

files = []
# if directory changes, have to adjust position for month and day below
for dirname, dirnames, filenames in os.walk('data/2013/'):
    for filename in filenames:
        if not filename.startswith('.'):
            files.append(os.path.join(dirname, filename))

files.sort()

first = True
with open('smile_data.csv', 'wb') as csvfile:
    # csvfile.write((u'\uFEFF\n').encode('utf-8'))
    csvwriter = csv.writer(csvfile, dialect=csv.excel)
    csvwriter.writerow(['id',
                        'id_str',
                        'text',
                        'in_reply_to_status_id',
                        'in_reply_to_screen_name',
                        'in_reply_to_user_id',
                        'created_at',
                        'user_id',
                        'user_id_str',
                        'user_name',
                        'user_screen_name',
                        'followers_count',
                        'user_location'
                        'statuses_count',
                        'user_description',
                        'friends_count',
                        'user_url',
                        'user_created_at',
                        'time_zone'])

    for file in files:
        # allow limiting to time range - example is 3 August through to 8 September
        # print file
        # adjust these if using different data path
        month = int(file[10:12])
        day = int(file[13:15])
        # print month, day
        if (start_month == 0) or (month == start_month and day >= start_day) or (month == end_month and day <= end_day):
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
                        continue
                    if first:
                        print '\nAvailable tweet keys:', tweet.keys()
                        print "\nAvailable tweet['user'] keys:", tweet['user'].keys(), '\n'
                        first = False
                    if tweet['id'] in deleted_tweet_ids:
                        print "skipped deleted tweet", tweet['id']
                    else:
                        if keyword in tweet['text']:
                            # having the twitter id as a number causes problems with excel using E notation and truncating the significant digits, so save str one as alternate
                            csvwriter.writerow([tweet['id'],
                                                '\''+tweet['id_str'],
                                                utf8_encode(tweet['text']),
                                                tweet['in_reply_to_status_id'],
                                                tweet['in_reply_to_screen_name'],
                                                tweet['in_reply_to_user_id'],
                                                tweet['created_at'],
                                                tweet['user']['id'],
                                                '\''+tweet['user']['id_str'],
                                                utf8_encode(tweet['user']['name']),
                                                utf8_encode(tweet['user']['screen_name']),
                                                tweet['user']['followers_count'],
                                                utf8_encode(tweet['user']['location']),
                                                tweet['user']['statuses_count'],
                                                utf8_encode(tweet['user']['description']),
                                                tweet['user']['friends_count'],
                                                tweet['user']['url'],
                                                tweet['user']['created_at'],
                                                utf8_encode(tweet['user']['time_zone'])])

end_time = time.time()
print "Elapsed time: " + str(end_time - start_time) + " seconds"
