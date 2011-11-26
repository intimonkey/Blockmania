package com.github.begla.blockmania.tests;


import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.yaml.snakeyaml.Yaml;

import com.github.begla.blockmania.persistence.SaveHandler;



public class SaveHandlerTests extends junit.framework.TestCase {

	private SaveHandler _handler;
	private File _file;
	
	protected void setUp() throws Exception{
		_file = File.createTempFile("save_handler", "test_file");
		FileOutputStream fos = new FileOutputStream(_file);
		fos.write("player_spawn_point: {x: 3, y: 4}\ntest: 3\nname: pants".getBytes());
		fos.close();
		
		_handler = new SaveHandler(_file.getPath());
	}
	public void testNestedMapFancyAccessor(){
		assertEquals(new Integer(3), _handler.<Integer>get("player_spawn_point.x", 4));
	}
	public void testNestedMapFancyAccessorY(){
		assertEquals(new Integer(4), _handler.<Integer>get("player_spawn_point.y", 33));
	}
	
	public void testMapDirect(){
		assertEquals(new Integer(3), _handler.get("player_spawn_point", (LinkedHashMap<String, Object>) null).get("x"));
	}
	public void testNestedMapNotPresent(){
		assertEquals(new Integer(4), _handler.<Integer>get("player_spawn_point.xddks", 4));
	}
	public void testGetMap(){
		assertEquals(LinkedHashMap.class, _handler.<Map>get("player_spawn_point", new LinkedHashMap()).getClass());
	}
	public void testDirect(){
		assertEquals(new Integer(3), _handler.<Integer>get("test", 4));
	}
	public void testName(){
		assertEquals("pants", _handler.<String>get("name", "jeans"));
	}
	
	public void testDefault(){
		assertEquals((Integer) 1, _handler.get("something", 1));
	}
	
	public void testCanAskForNull(){
		assertEquals(null, _handler.get("something", null));
	}
	
	public void testFloat() throws Exception{
		SaveHandler h = new SaveHandler(_file.getPath());
		Object val = h.get("prop", null);
		assertNull(val);
		h.set("prop", 1.0);
		h.save();
		SaveHandler nh = new SaveHandler(_file.getPath());
		assertEquals(1.0, nh.get("prop", 4));		
	}
	
	public void testWithoutFile() throws Exception{
		_file = File.createTempFile("stuff", "pants");
		assert(_file.delete());
		SaveHandler h = new SaveHandler(_file.getPath());
		Object val = h.get("prop", null);
		assertNull(val);
		h.set("prop", 1);
		h.save();
		SaveHandler nh = new SaveHandler(_file.getPath());
		assertEquals((Integer)1, nh.get("prop", 4));
	}
	
	public void testAdd() throws Exception{
		_handler.set("new_value", 3);
		_handler.save();
		assertEquals(new Integer(3), _handler.<Integer>get("test", 4));
		assertEquals("pants", _handler.<String>get("name", "jeans"));
		assertEquals(new Integer(3), _handler.<Integer>get("player_spawn_point.x", 4));
		assertEquals(new Integer(4), _handler.<Integer>get("player_spawn_point.y", 43));
		assertEquals(new Integer(3), _handler.<Integer>get("new_value", 4));
	}
	
	public void testCanHandleVector3f(){
		Yaml yaml = new Yaml();
		Vector3f v = new Vector3f(1,1,1);
		String ser = yaml.dump(v);
		Vector3f thing = (Vector3f) yaml.load(ser);
		assertEquals(v, thing);
	}
	
	public void tearDown(){
		assert(_file.delete());
	}
	
}
