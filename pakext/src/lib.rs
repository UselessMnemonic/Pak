mod zlib;

use zlib::ZStream;
use std::ptr::null_mut;

#[no_mangle]
pub unsafe extern "C" fn heap_deflate(stream: *mut ZStream,
                                      next_in: *const u8, next_out: *mut u8,
                                      avail_in: u32, avail_out: u32,
                                      flush: i32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.next_in = next_in;
    stream.avail_in = avail_in;
    stream.next_out = next_out;
    stream.avail_out = avail_out;
    let result = stream.deflate(flush);

    stream.next_in = null_mut();
    stream.avail_in = 0;
    stream.next_out = null_mut();
    stream.avail_out = 0;
    return result;
}

#[no_mangle]
pub unsafe extern "C" fn heap_deflate_params(stream: *mut ZStream,
                                             next_in: *const u8, next_out: *mut u8,
                                             avail_in: u32, avail_out: u32,
                                             level: i32, strategy: i32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.next_in = next_in;
    stream.avail_in = avail_in;
    stream.next_out = next_out;
    stream.avail_out = avail_out;
    let result = stream.deflate_params(level, strategy);

    stream.next_in = null_mut();
    stream.avail_in = 0;
    stream.next_out = null_mut();
    stream.avail_out = 0;
    return result;
}

#[no_mangle]
pub unsafe extern "C" fn heap_inflate(stream: *mut ZStream,
                                      next_in: *const u8, next_out: *mut u8,
                                      avail_in: u32, avail_out: u32,
                                      flush: i32) -> i32 {
    let stream = stream.as_mut().unwrap();
    stream.next_in = next_in;
    stream.avail_in = avail_in;
    stream.next_out = next_out;
    stream.avail_out = avail_out;
    let result = stream.inflate(flush);

    stream.next_in = null_mut();
    stream.avail_in = 0;
    stream.next_out = null_mut();
    stream.avail_out = 0;
    return result;
}
