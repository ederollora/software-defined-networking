import os
import grequests

# generate traget set of urls
urls = [
 'http://10.0.0.100:8000/1kb.zip',
 ]

# send requests simultaneously to the targets in url
rs = (grequests.get(u) for u in urls)

#get responses to requests ac
grequests.map(rs)
 

