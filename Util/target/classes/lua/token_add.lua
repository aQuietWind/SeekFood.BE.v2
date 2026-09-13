local key = KEYS[1]
local max = tonumber(ARGV[1])
local token = ARGV[2]
local score = tonumber(ARGV[3])

-- 判断当前列表长度
local len = redis.call('ZCARD', key)
if len >= max then
    redis.call('ZREMRANGEBYRANK', key,0,0)
end
-- 追加新token到尾部
redis.call('ZADD', key,score, token)
return true