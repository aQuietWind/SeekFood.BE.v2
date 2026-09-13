local key = KEYS[1]
local max = tonumber(ARGV[1])
local token = ARGV[2]
local score = tonumber(ARGV[3])

-- 判断当前列表长度
local len = redis.call('zremrangebyrank', key,0,100)
-- 追加新token到尾部
redis.call('ZADD', key,score, token)
return true