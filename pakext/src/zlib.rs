use libloading::Library;
use std::ffi::{c_ulong, c_void, c_char, OsString};
use std::env;
use std::alloc::{alloc_zeroed, Layout};
use std::ops::Deref;

/** Bindings for ZLib */

type ZAllocFn = unsafe extern "C" fn(opaque: *c_void, items: u32, size: u32) -> *const c_void;
type ZFreeFn = unsafe extern "C" fn(opaque: *c_void, address: *c_void) -> c_void;

#[repr(C)]
pub struct ZStreamImpl {
    pub next_in: *const u8,
    pub avail_in: u32,
    pub total_in: c_ulong,

    pub next_out: *mut u8,
    pub avail_out: u32,
    pub total_out: c_ulong,

    pub msg: *const u8,
    pub state: *const c_void,

    pub zalloc: Option<ZAllocFn>,
    pub zfree: Option<ZFreeFn>,
    pub opaque: *const c_void,

    pub data_type: u32,
    pub adler: c_ulong,
    pub reserved: c_ulong,
}

#[repr(transparent)]
#[derive(Debug)]
pub struct ZStream {
    internal: *mut ZStreamImpl
}

type ZLibVersionFn = unsafe extern "C" fn() -> *const c_char;

type ZDeflateInitFn = unsafe extern "C" fn(stream: ZStream, level: i32, version: *const u8, size: u32) -> i32;
type ZDeflateParamsFn = unsafe extern "C" fn(stream: ZStream, level: i32, strategy: i32) -> i32;
type ZDeflateGetDictionaryFn = unsafe extern "C" fn(stream: ZStream, dictionary: *mut u8, size: *mut u32) -> i32;
type ZDeflateSetDictionaryFn = unsafe extern "C" fn(stream: ZStream, dictionary: *const u8, size: u32) -> i32;
type ZDeflateFn = unsafe extern "C" fn(stream: ZStream, flush: i32) -> i32;
type ZDeflateResetFn = unsafe extern "C" fn(stream: ZStream) -> i32;
type ZDeflateEndFn = unsafe extern "C" fn(stream: ZStream) -> i32;

type ZInflateInitFn = unsafe extern "C" fn(stream: ZStream, level: i32, version: *const u8, size: u32) -> i32;
type ZInflateParamsFn = unsafe extern "C" fn(stream: ZStream, level: i32, strategy: i32) -> i32;
type ZInflateGetDictionaryFn = unsafe extern "C" fn(stream: ZStream, dictionary: *mut u8, size: *mut u32) -> i32;
type ZInflateSetDictionaryFn = unsafe extern "C" fn(stream: ZStream, dictionary: *const u8, size: u32) -> i32;
type ZInflateFn = unsafe extern "C" fn(stream: ZStream, flush: i32) -> i32;
type ZInflateResetFn = unsafe extern "C" fn(stream: ZStream) -> i32;
type ZInflateEndFn = unsafe extern "C" fn(stream: ZStream) -> i32;

static ZLIB_LIBRARY: OsString = match env::var("ZLIB_LIBRARY") {
    Ok(path) => OsString::from(path),
    Err(cause) => match(cause) {
        env::VarError::NotPresent => {
            #[cfg(target_os = "windows")]
            return OsString::from("zlib1");
            #[cfg(not(target_os = "windows"))]
            return OsString::from("libz")
        }
        env::VarError::NotUnicode(it) => it
    }
};

static ZLIB: Library = unsafe { Library::new(ZLIB_LIBRARY).unwrap() };
static ZLIB_VERSION: *const c_char = unsafe { ZLIB.get::<ZLibVersionFn>(b"zlibVersion\0").unwrap()() };
static Z_STREAM_LAYOUT: Layout = Layout::new::<ZStream>();

static Z_DEFLATE_INIT: ZDeflateInitFn = unsafe { ZLIB.get(b"_deflateInit\0").unwrap() };
static Z_DEFLATE_PARAMS: ZDeflateParamsFn = unsafe { ZLIB.get(b"deflateParams\0").unwrap() };
static Z_DEFLATE_GET_DICTIONARY: ZDeflateGetDictionaryFn = unsafe { ZLIB.get(b"deflateGetDictionary\0").unwrap() };
static Z_DEFLATE_SET_DICTIONARY: ZDeflateSetDictionaryFn = unsafe { ZLIB.get(b"deflateSetDictionary\0").unwrap() };
static Z_DEFLATE: ZDeflateFn = unsafe { ZLIB.get(b"deflate\0").unwrap() };
static Z_DEFLATE_RESET: ZDeflateResetFn = unsafe { ZLIB.get(b"deflateReset\0").unwrap() };
static Z_DEFLATE_END: ZDeflateEndFn = unsafe { ZLIB.get(b"deflateEnd\0").unwrap() };

static Z_INFLATE_INIT: ZInflateInitFn = unsafe { ZLIB.get(b"_inflateInit\0").unwrap() };
static Z_INFLATE_PARAMS: ZInflateParamsFn = unsafe { ZLIB.get(b"inflateParams\0").unwrap() };
static Z_INFLATE_GET_DICTIONARY: ZInflateGetDictionaryFn = unsafe { ZLIB.get(b"inflateGetDictionary\0").unwrap() };
static Z_INFLATE_SET_DICTIONARY: ZInflateSetDictionaryFn = unsafe { ZLIB.get(b"inflateSetDictionary\0").unwrap() };
static Z_INFLATE: ZInflateFn = unsafe { ZLIB.get(b"inflate\0").unwrap() };
static Z_INFLATE_RESET: ZInflateResetFn = unsafe { ZLIB.get(b"inflateReset\0").unwrap() };
static Z_INFLATE_END: ZInflateEndFn = unsafe { ZLIB.get(b"inflateEnd\0").unwrap() };

impl Into<u64> for ZStream {
    pub fn into(self) -> u64 {
        unsafe {
            self.internal as u64
        }
    }
}

impl From<u64> for ZStream {
    pub fn from(value: u64) -> Self {
        unsafe {
            ZStream {
                internal: value as *mut ZStreamImpl
            }
        }
    }
}

impl Deref for ZStream {
    type Target = *mut ZStreamImpl;

    fn deref(self) -> Self::Target {
        self.internal
    }
}

impl ZStream {
    pub fn new() -> Self {
        unsafe {
            Self {
                internal: alloc_zeroed(Z_STREAM_LAYOUT) as *mut ZStream
            }
        }
    }

    pub fn deflate_init(self, level: i32) -> i32 {
        unsafe {
            Z_DEFLATE_INIT(self, level, ZLIB_VERSION, size_of::<ZStreamImpl>())
        }
    }

    pub fn deflate_params(self, level: i32, strategy: i32) -> i32 {
        unsafe {
            Z_DEFLATE_PARAMS(self, level, strategy)
        }
    }

    pub fn deflate_get_dictionary(self, dictionary: *const u8, size: *mut u32) -> i32 {
        unsafe {
            Z_DEFLATE_GET_DICTIONARY(self, dictionary, size)
        }
    }

    pub fn deflate_set_dictionary(self, dictionary: *const u8, size: u32) -> i32 {
        unsafe {
            Z_DEFLATE_SET_DICTIONARY(self, dictionary, size)
        }
    }

    pub fn deflate(self, flush: i32) -> i32 {
        unsafe {
            Z_DEFLATE(self, flush)
        }
    }

    pub fn deflate_reset(self) -> i32 {
        unsafe {
            Z_DEFLATE_RESET(self)
        }
    }

    pub fn deflate_end(self) -> i32 {
        unsafe {
            Z_DEFLATE_END(self)
        }
    }

    pub fn inflate_init(self, level: i32) -> i32 {
        unsafe {
            Z_INFLATE_INIT(self, level, ZLIB_VERSION, size_of::<ZStreamImpl>())
        }
    }

    pub fn inflate_get_dictionary(self, dictionary: *const u8, size: *mut u32) -> i32 {
        unsafe {
            Z_INFLATE_GET_DICTIONARY(self, dictionary, size)
        }
    }

    pub fn inflate_set_dictionary(self, dictionary: *const u8, size: u32) -> i32 {
        unsafe {
            Z_INFLATE_SET_DICTIONARY(self, dictionary, size)
        }
    }

    pub fn inflate(self, flush: i32) -> i32 {
        unsafe {
            Z_INFLATE(self, flush)
        }
    }

    pub fn inflate_reset(self) -> i32 {
        unsafe {
            Z_INFLATE_RESET(self)
        }
    }

    pub fn inflate_end(self) -> i32 {
        unsafe {
            Z_INFLATE_END(self)
        }
    }
}
