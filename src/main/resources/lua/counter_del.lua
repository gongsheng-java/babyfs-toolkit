--[[
删除指定的counter key
参数:
sync_set_key,last_write_timestamp
返回值:
{1}
-- ]]
local counter_key = KEYS[1] --计数器key
local sync_set_key = ARGV[1] -- 同步集合key
redis.call("DEL", counter_key)
redis.call("ZREM", sync_set_key, counter_key)
return 1
