package com.fiftyonred.mock_jedis;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.*;

import static org.junit.Assert.*;

public class MockJedisTest {
	private Jedis j = null;

	@Before
	public void setUp() {
		j = new MockJedis("test");
	}

	@Test
	public void testSet() {
		assertEquals("OK", j.set("test", "123"));
	}

	@Test
	public void testGet() {
		j.set("test", "123");
		assertEquals("123", j.get("test"));
		assertEquals(null, j.get("unknown"));
	}

	@Test
	public void testHashes() {
		assertEquals(0L, j.hlen("test").longValue());
		assertEquals(0L, j.hdel("test", "name").longValue());
		assertEquals(null, j.hget("test", "name"));
		j.hset("test", "name", "value");
		final Set<String> keys = j.hkeys("test");
		final Map<String, String> entries = j.hgetAll("test");
		final List<String> vals = j.hvals("test");
		assertTrue(keys.contains("name"));
		assertEquals(1, keys.size());
		assertEquals(1, entries.size());
		assertEquals("value", entries.get("name"));
		assertTrue(vals.contains("value"));
		assertEquals(1, vals.size());
		assertTrue(j.hexists("test", "name"));
		assertFalse(j.hexists("test", "name2"));
		assertEquals(1L, j.hlen("test").longValue());
		assertEquals("value", j.hget("test", "name"));
		assertEquals(1L, j.hdel("test", "name").longValue());
	}

	@Test
	public void testSets() {
		assertFalse(j.sismember("test", "member 1"));

		assertEquals(2L, (long) j.sadd("test", "member 1", "member 2"));
		assertEquals(1L, (long) j.sadd("test", "member 3"));

		// duplicate member 1. should drop
		assertEquals(0L, (long) j.sadd("test", "member 1"));

		assertEquals(3, j.smembers("test").size());

		// should remove member 3
		assertEquals(1L, (long) j.srem("test", "member 3"));

		List<String> sortedMembers = new ArrayList<String>(2);
		sortedMembers.addAll(j.smembers("test"));
		Collections.sort(sortedMembers);

		assertEquals("member 1", sortedMembers.get(0));
		assertEquals("member 2", sortedMembers.get(1));
	}

	@Test
	public void testHincrBy() {
		j.hincrBy("test1", "name", 10);
		assertEquals("10", j.hget("test1", "name"));

		j.hincrBy("test1", "name", -2);
		assertEquals("8", j.hget("test1", "name"));

		j.hset("test1", "name", "5");
		j.hincrBy("test1", "name", 2);
		assertEquals("7", j.hget("test1", "name"));

		j.hincrByFloat("test1", "name", -0.5D);
		assertEquals("6.5", j.hget("test1", "name"));
	}

	@Test
	public void testList() {
		assertEquals(Long.valueOf(0), j.llen("test"));

		j.lpush("test", "a");
		j.lpush("test", "b");
		j.lpush("test", "c");

		assertEquals(Long.valueOf(3), j.llen("test"));

		assertEquals("c", j.lpop("test"));
		assertEquals("b", j.lpop("test"));
		assertEquals("a", j.lpop("test"));

		assertEquals(Long.valueOf(0), j.llen("test"));

        j.lpush("test", "a");
        j.lpush("test", "b");
        j.lpush("test", "c");

        assertEquals(Long.valueOf(3), j.llen("test"));

        assertEquals("a", j.rpop("test"));
        assertEquals("b", j.rpop("test"));
        assertEquals("c", j.rpop("test"));

        assertEquals(Long.valueOf(0), j.llen("test"));

        j.rpush("test", "x");
        j.rpush("test", "y");
        j.rpush("test", "z");

        assertEquals(Long.valueOf(3), j.llen("test"));

        assertEquals("x", j.lpop("test"));
        assertEquals("y", j.lpop("test"));
        assertEquals("z", j.lpop("test"));

        assertEquals(Long.valueOf(0), j.llen("test"));

        j.rpush("test", "x");
        j.rpush("test", "y");
        j.rpush("test", "z");

        assertEquals(Long.valueOf(3), j.llen("test"));

        assertEquals("z", j.rpop("test"));
        assertEquals("y", j.rpop("test"));
        assertEquals("x", j.rpop("test"));

        assertEquals(Long.valueOf(0), j.llen("test"));
	}

	@Test
	public void testListRemoval() {
		j.rpush("test", "x");
		j.rpush("test", "x");
		j.rpush("test", "y");
		j.rpush("test", "x");
		// test removal starting at the tail
		j.lrem("test", -2, "x");
		List<String> list = j.lrange("test", 0, -1);
		assertEquals(2, list.size());
		assertEquals("x", list.get(0));
		assertEquals("y", list.get(1));
		j.del("test");

		j.rpush("test", "x");
		j.rpush("test", "x");
		j.rpush("test", "y");
		j.rpush("test", "x");
		// test removal starting at the head
		j.lrem("test", 2, "x");
		list = j.lrange("test", 0, -1);
		assertEquals(2, list.size());
		assertEquals("y", list.get(0));
		assertEquals("x", list.get(1));
		j.del("test");
	}

    @Test
    public void testMixedListPush() {
        assertEquals(Long.valueOf(0), j.llen("test"));

        j.lpush("test", "a");
        j.rpush("test", "x");
        j.lpush("test", "b");
        j.rpush("test", "y");
        j.lpush("test", "c");
        j.rpush("test", "z");

        assertEquals(Long.valueOf(6), j.llen("test"));

        assertEquals("c", j.lpop("test"));
        assertEquals("b", j.lpop("test"));
        assertEquals("a", j.lpop("test"));
        assertEquals("x", j.lpop("test"));
        assertEquals("y", j.lpop("test"));
        assertEquals("z", j.lpop("test"));

        assertEquals(Long.valueOf(0), j.llen("test"));

        j.lpush("test", "a");
        j.rpush("test", "x");
        j.lpush("test", "b");
        j.rpush("test", "y");
        j.lpush("test", "c");
        j.rpush("test", "z");

        assertEquals(Long.valueOf(6), j.llen("test"));

        assertEquals("z", j.rpop("test"));
        assertEquals("y", j.rpop("test"));
        assertEquals("x", j.rpop("test"));
        assertEquals("a", j.rpop("test"));
        assertEquals("b", j.rpop("test"));
        assertEquals("c", j.rpop("test"));

        assertEquals(Long.valueOf(0), j.llen("test"));

        j.lpush("test", "a");
        j.rpush("test", "x");
        j.lpush("test", "b");
        j.rpush("test", "y");
        j.lpush("test", "c");
        j.rpush("test", "z");

        assertEquals(Long.valueOf(6), j.llen("test"));

        assertEquals("z", j.rpop("test"));
        assertEquals("c", j.lpop("test"));
        assertEquals("y", j.rpop("test"));
        assertEquals("b", j.lpop("test"));
        assertEquals("x", j.rpop("test"));
        assertEquals("a", j.lpop("test"));

        assertEquals(Long.valueOf(0), j.llen("test"));
    }

	@Test
	public void testLRange() {
		j.lpush("test", "a");
		j.lpush("test", "b");
		j.lpush("test", "c");
		j.lpush("test", "d");

		assertEquals(Arrays.asList("d", "c"), j.lrange("test", 0, 1));
		assertEquals(Arrays.asList("b", "a"), j.lrange("test", 2, 5));
		assertEquals(Arrays.asList("b", "a"), j.lrange("test", -2, -1));
		assertEquals(Arrays.asList("b"), j.lrange("test", -2, -2));
		assertEquals(0, j.lrange("test", -7, -6).size());
		assertEquals(0, j.lrange("test", 6, 7).size());
	}

	@Test
	public void testSort() {
		j.lpush("test", "a");
		j.lpush("test", "c");
		j.lpush("test", "b");
		j.lpush("test", "d");

		try {
			j.sort("test");
			fail("Sorting numbers is default");
		} catch (JedisDataException e) {
		}

		assertEquals(Arrays.asList("a", "b", "c", "d"), j.sort("test", new SortingParams().alpha()));
		assertEquals(Arrays.asList("d", "c", "b", "a"), j.sort("test", new SortingParams().desc().alpha()));

		j.sort("test", new SortingParams().alpha(), "newkey");

		assertEquals(Arrays.asList("a", "b", "c", "d"), j.lrange("newkey", 0, 10));

		j.sadd("settest", "1", "2", "3", "4", "5", "6");

		assertEquals(Arrays.asList("1", "2", "3", "4", "5", "6"), j.sort("settest"));
		assertEquals(Arrays.asList("3", "4", "5"), j.sort("settest", new SortingParams().limit(2, 3)));
		assertEquals(Arrays.asList("4", "3", "2"), j.sort("settest", new SortingParams().limit(2, 3).desc()));
	}

	@Test(expected = JedisDataException.class)
	public void testInvalidKeyTypeHashToString() {
		j.hset("test", "test", "1");
		j.get("test");
	}

	@Test(expected = JedisDataException.class)
	public void testInvalidKeyTypeHashToList() {
		j.hset("test", "test", "1");
		j.llen("test");
	}

	@Test(expected = JedisDataException.class)
	public void testInvalidKeyTypeStringToHash() {
		j.set("test", "test");
		j.hget("test", "test");
	}

	@Test(expected = JedisDataException.class)
	public void testInvalidKeyTypeStringToList() {
		j.set("test", "test");
		j.lpop("test");
	}

	@Test(expected = JedisDataException.class)
	public void testInvalidKeyTypeListToHash() {
		j.lpush("test", "test");
		j.hgetAll("test");
	}

	@Test(expected = JedisDataException.class)
	public void testInvalidKeyTypeListToString() {
		j.lpush("test", "test");
		j.incr("test");
	}

	@Test
	public void testKeys() {
		j.set("A1", "value");
		j.set("A2", "value");
		j.set("A3", "value");
		j.hset("B1", "name", "value");
		j.hset("B2", "name", "value");
		j.hset("C2C", "name", "value");

		assertEquals(6, j.keys("*").size());
		assertEquals(1, j.keys("A1").size());
		assertEquals(3, j.keys("A*").size());
		assertEquals(2, j.keys("*1").size());
		assertEquals(3, j.keys("*2*").size());
		assertEquals(1, j.keys("C*C").size());

		j.set("testC2C", "value");
		assertEquals(1, j.keys("C*C").size());
	}

	@Test
	public void testMultipleDB() {
		assertEquals(0L, j.dbSize().longValue());
		j.set("test", "test");
		assertEquals(1L, j.dbSize().longValue());
		j.move("test", 5);
		assertEquals(0L, j.dbSize().longValue());
		j.select(5);
		assertEquals(1L, j.dbSize().longValue());
	}

	@Test
	public void testZCount() {
		j.zcount("foo", 0, 1);
	}

  @Test
  public void testRpoplpush() {
    j.rpush("list1", "abc");
    j.rpush("list1", "def");
    String element = j.rpoplpush("list1", "list2");
    assertEquals("def", element);

    assertEquals(1L, j.llen("list1").longValue());
    assertEquals(1L, j.llen("list2").longValue());

    element = j.rpoplpush("list1", "list2");
    assertEquals("abc", element);
    
    assertEquals(0L, j.llen("list1").longValue());
    assertEquals(2L, j.llen("list2").longValue());
    
    element = j.rpoplpush("list1", "list2");
    assertNull(element);
    
    assertEquals("def", j.rpop("list2"));
    assertEquals("abc", j.rpop("list2"));
    assertNull(j.rpop("list2"));
  }
  
  /**
   * Test RPOPLPUSH with srckey == dstkey
   */
  @Test
  public void testRpoplpushCycle() {
    j.rpush("list1", "abc");
    j.rpush("list1", "def");
    assertEquals("def", j.rpoplpush("list1", "list1"));
    assertEquals(2L, j.llen("list1").longValue());
    assertEquals("abc", j.rpoplpush("list1", "list1"));
    assertEquals(2L, j.llen("list1").longValue());
    assertEquals("def", j.rpoplpush("list1", "list1"));
    assertEquals(2L, j.llen("list1").longValue());
    
    assertEquals("abc", j.rpop("list1"));
    assertEquals("def", j.rpop("list1"));
    assertNull(j.rpop("list2"));
  }

}
