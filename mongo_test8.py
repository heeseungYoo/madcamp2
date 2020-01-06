#! /usr/bin/python
from pymongo import MongoClient, ReadPreference
from sshtunnel import SSHTunnelForwarder
import paramiko
from datetime import datetime, timedelta


# SSH통해서 mongodb접속하기 
SSH_KEY_LOCATION = 'C:/Users/q/Downloads/cs496-key.pem' 
JUMP_MACHINE_ADDRESS = '192.249.19.252'
SSH_USER = 'root'
REMOTE_MONGO_ADDRESS = '127.0.0.1'

DB_NAME = 'attendance'
COLLECTION_NAME = 'students'

pkey = paramiko.RSAKey.from_private_key_file(SSH_KEY_LOCATION)
server = SSHTunnelForwarder(
    (JUMP_MACHINE_ADDRESS, 1722),
    ssh_username=SSH_USER,
    ssh_private_key=pkey,
    remote_bind_address=(REMOTE_MONGO_ADDRESS, 27017),
    local_bind_address=('0.0.0.0', 27017)
)

# 서버 접속 시작 
server.start()

# mongodb 접속 
client = MongoClient('mongodb://127.0.0.1:27017')
db = client[DB_NAME]
col = db[COLLECTION_NAME]

'''
# student_id '20171234'의 모든 lecture 'CS777'로 변경 
col.update_many(
    {'student_id': "20171234"}, 
    {"$set": {'lecture': "CS777"}}
    )

# student_id와 lecture 둘 다 맞으면 출석 체크 Y
col.update(
    {"$and":[{'student_id': '20181234'}, {'lecture' : 'CS203'}]}, 
    {"$set": {'atd_check': 'Y'}}
    )   
'''

docs = col.find()

# collection 전체 출력 
for i in docs:
    print(i) 


print("----------------------------------------------------------")    

classroom = 'N1_221'
lecture = {'CS101':'2020:01:06:01:20:00', 'CS122':'2020:01:06:00:00:00', 'CS201':'2020:01:07:15:00:00'}

#lecture_time = '2020:01:05:23:20:00'
#result = lecture_time.split(':')
#lecture_time_result = datetime(int(result[0]), int(result[1]), int(result[2]), int(result[3]), int(result[4]), int(result[5]))
#print((lecture_time_result))

keys = list(lecture.keys())
for key in keys:
    result = lecture[key].split(':')
    lecture_time_result = datetime(int(result[0]), int(result[1]), int(result[2]), int(result[3]), int(result[4]), int(result[5]))
    lecture[key] = lecture_time_result
print("---------------------------")
print(lecture.items())
values = list(lecture.values())
time = datetime.now()
for value in values:
    print(value)
    print(time)
    print("---------------------------")
    difference = (value-time).seconds / 60
    print(difference)
    if (difference < 30.0):
        val = value.strftime('%H:%M')
        print(val)
        col.update(
        {"$and":[{'student_id': '20181234'}, {'lecture_start_time' : val}]}, 
        {"$set": {'atd_check': 'Y'}}
        )
    else:
        break

#time = datetime.now()
#difference = (lecture_time_result-time).seconds / 60
#if (difference < 30.0) :
#    col.update(
#    {"$and":[{'student_id': '20181234'}, {'lecture' : 'CS201'}]}, 
#    {"$set": {'atd_check': 'N'}}
#    ) 

docs = col.find()

for i in docs:
    print(i)


# 서버 접속 종료
server.stop()