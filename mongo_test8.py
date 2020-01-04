#! /usr/bin/python
from pymongo import MongoClient, ReadPreference
from sshtunnel import SSHTunnelForwarder
import paramiko


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

# student_id '20171234'의 모든 lecture 'CS777'로 변경 
col.update_many(
    {'student_id': "20171234"}, 
    {"$set": {'lecture': "CS777"}}
    )
docs = col.find()

# collection 전체 출력 
for i in docs:
    print(i)

# 서버 접속 종료
server.stop()