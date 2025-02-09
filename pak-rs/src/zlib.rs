use std::ffi::{c_char, c_ulong, c_void, OsString};
use std::env;
use std::ptr::{null, null_mut};
use std::sync::LazyLock;
use libloading::{Library, Symbol};

/** Bindings for ZLib */

type ZAllocFn = unsafe extern "C" fn(opaque: *mut c_void, items: u32, size: u32) -> *const c_void;
type ZFreeFn = unsafe extern "C" fn(opaque: *mut c_void, address: *mut c_void) -> c_void;
type ZLibVersionFn = unsafe extern "C" fn() -> *const c_char;

#[repr(C)]
#[derive(Debug)]
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
    pub reserved: c_ulong
}

impl Default for ZStreamImpl {
    fn default() -> Self {
        Self {
            next_in: null(),
            avail_in: 0,
            total_in: 0,
            next_out: null_mut(),
            avail_out: 0,
            total_out: 0,
            msg: null(),
            state: null(),
            zalloc: None,
            zfree: None,
            opaque: null(),
            data_type: 0,
            adler: 0,
            reserved: 0
        }
    }
}

pub struct ZStream(*mut ZStreamImpl);

type ZDeflateInitFn = unsafe extern "C" fn(stream: ZStream, level: i32, version: *const c_char, size: u32) -> i32;
type ZDeflateParamsFn = unsafe extern "C" fn(stream: ZStream, level: i32, strategy: i32) -> i32;
type ZDeflateGetDictionaryFn = unsafe extern "C" fn(stream: ZStream, dictionary: *mut u8, size: *mut u32) -> i32;
type ZDeflateSetDictionaryFn = unsafe extern "C" fn(stream: ZStream, dictionary: *const u8, size: u32) -> i32;
type ZDeflateFn = unsafe extern "C" fn(stream: ZStream, flush: i32) -> i32;
type ZDeflateResetFn = unsafe extern "C" fn(stream: ZStream) -> i32;
type ZDeflateEndFn = unsafe extern "C" fn(stream: ZStream) -> i32;

type ZInflateInitFn = unsafe extern "C" fn(stream: ZStream, version: *const c_char, size: u32) -> i32;
type ZInflateGetDictionaryFn = unsafe extern "C" fn(stream: ZStream, dictionary: *mut u8, size: *mut u32) -> i32;
type ZInflateSetDictionaryFn = unsafe extern "C" fn(stream: ZStream, dictionary: *const u8, size: u32) -> i32;
type ZInflateFn = unsafe extern "C" fn(stream: ZStream, flush: i32) -> i32;
type ZInflateResetFn = unsafe extern "C" fn(stream: ZStream) -> i32;
type ZInflateEndFn = unsafe extern "C" fn(stream: ZStream) -> i32;

#[derive(Debug)]
struct ZLib<'z> {
    library: Library,
    zlib_version: *const c_char,

    deflate_init: Symbol<'z, ZDeflateInitFn>,
    deflate_params: Symbol<'z, ZDeflateParamsFn>,
    deflate_get_dictionary: Symbol<'z, ZDeflateGetDictionaryFn>,
    deflate_set_dictionary: Symbol<'z, ZDeflateSetDictionaryFn>,
    deflate: Symbol<'z, ZDeflateFn>,
    deflate_reset: Symbol<'z, ZDeflateResetFn>,
    deflate_end: Symbol<'z, ZDeflateEndFn>,

    inflate_init: Symbol<'z, ZInflateInitFn>,
    inflate_get_dictionary: Symbol<'z, ZInflateGetDictionaryFn>,
    inflate_set_dictionary: Symbol<'z, ZInflateSetDictionaryFn>,
    inflate: Symbol<'z, ZInflateFn>,
    inflate_reset: Symbol<'z, ZInflateResetFn>,
    inflate_end: Symbol<'z, ZInflateEndFn>
}

impl<'z> ZLib<'z> {
    fn new() -> Self {
        let path = match env::var("ZLIB_LIBRARY") {
            Ok(path) => OsString::from(path),
            Err(cause) => match cause {
                #[cfg(target_os = "windows")]
                env::VarError::NotPresent => OsString::from("zlib1"),
                #[cfg(not(target_os = "windows"))]
                env::VarError::NotPresent => OsString::from("libz"),
                env::VarError::NotUnicode(it) => it
            }
        };
        unsafe {
            let library = Library::new(path).unwrap();
            let zlib_version = library.get::<ZLibVersionFn>(b"zlibVersion\0").expect("Could not find zlibVersion")();

            let deflate_init = library.get::<ZDeflateInitFn>(b"deflateInit_\0").expect("Could not find deflateInit_");
            let deflate_params = library.get::<ZDeflateParamsFn>(b"deflateParams\0").expect("Could not find deflateParams");
            let deflate_get_dictionary = library.get::<ZDeflateGetDictionaryFn>(b"deflateGetDictionary\0").expect("Could not find deflateGetDictionary");
            let deflate_set_dictionary = library.get::<ZDeflateSetDictionaryFn>(b"deflateSetDictionary\0").expect("Could not find deflateSetDictionary");
            let deflate = library.get::<ZDeflateFn>(b"deflate\0").expect("Could not find deflate");
            let deflate_reset = library.get::<ZDeflateResetFn>(b"deflateReset\0").expect("Could not find deflateReset");
            let deflate_end = library.get::<ZDeflateEndFn>(b"deflateEnd\0").expect("Could not find deflateEnd");

            let inflate_init = library.get::<ZInflateInitFn>(b"inflateInit_\0").expect("Could not find inflateInit_");
            let inflate_get_dictionary = library.get::<ZInflateGetDictionaryFn>(b"inflateGetDictionary\0").expect("Could not find inflateGetDictionary");
            let inflate_set_dictionary = library.get::<ZInflateSetDictionaryFn>(b"inflateSetDictionary\0").expect("Could not find inflateSetDictionary");
            let inflate = library.get::<ZInflateFn>(b"inflate\0").expect("Could not find inflate");
            let inflate_reset = library.get::<ZInflateResetFn>(b"inflateReset\0").expect("Could not find inflateReset");
            let inflate_end = library.get::<ZInflateEndFn>(b"inflateEnd\0").expect("Could not find inflateEnd");

            Self {
                library,
                zlib_version,
                deflate_init,
                deflate_params,
                deflate_get_dictionary,
                deflate_set_dictionary,
                deflate,
                deflate_reset,
                deflate_end,
                inflate_init,
                inflate_get_dictionary,
                inflate_set_dictionary,
                inflate,
                inflate_reset,
                inflate_end
            }
        }
    }
}

unsafe impl<'z> Sync for ZLib<'z> {}
unsafe impl<'z> Send for ZLib<'z> {}

static ZLIB: LazyLock<ZLib<'static>> = LazyLock::new(ZLib::new);

impl ZStream {

    pub fn new() -> Self {
        Box::into_raw(Box::new(ZStreamImpl::default()))
    }

    pub fn deflate_init(self, level: i32) -> i32 {
        (ZLIB.deflate_init)(self, level, ZLIB.zlib_version, size_of::<Self>() as u32)
    }

    pub fn deflate_params(self, level: i32, strategy: i32) -> i32 {
        (ZLIB.deflate_params)(self, level, strategy)
    }

    pub fn deflate_params_ext(self, next_in: *const u8, next_out: *mut u8,
                                    avail_in: u32, avail_out: u32,
                                    level: i32, strategy: i32) -> i32 {
        unsafe {
            (*self.0).next_in = next_in;
            (*self.0).avail_in = avail_in;
            (*self.0).next_out = next_out;
            (*self.0).avail_out = avail_out;
            let result = (ZLIB.deflate_params)(self.0, level, strategy);

            (*self.0).next_in = null_mut();
            (*self.0).avail_in = 0;
            (*self.0).next_out = null_mut();
            (*self.0).avail_out = 0;
            result
        }
    }

    pub fn deflate_get_dictionary(self, dictionary: *mut u8, size: *mut u32) -> i32 {
        (ZLIB.deflate_get_dictionary)(self, dictionary, size)
    }

    pub fn deflate_set_dictionary(self, dictionary: *const u8, size: u32) -> i32 {
        (ZLIB.deflate_set_dictionary)(self, dictionary, size)
    }

    pub fn deflate(self, flush: i32) -> i32 {
        (ZLIB.deflate)(self, flush)
    }

    pub fn deflate_ext(self: ZStream, next_in: *const u8, next_out: *mut u8,
                                      avail_in: u32, avail_out: u32,
                                      flush: i32) -> i32 {
        unsafe {
            (*self.0).next_in = next_in;
            (*self.0).avail_in = avail_in;
            (*self.0).next_out = next_out;
            (*self.0).avail_out = avail_out;
            let result = (ZLIB.deflate)(self.0, flush);

            (*self.0).next_in = null_mut();
            (*self.0).avail_in = 0;
            (*self.0).next_out = null_mut();
            (*self.0).avail_out = 0;
            result
        }
    }

    pub fn deflate_reset(self) -> i32 {
        (ZLIB.deflate_reset)(self)
    }

    pub fn deflate_end(self) -> i32 {
        (ZLIB.deflate_end)(self)
    }

    pub fn inflate_init(self) -> i32 {
        (ZLIB.inflate_init)(self, ZLIB.zlib_version, size_of::<Self>() as u32)
    }

    pub fn inflate_get_dictionary(self, dictionary: *mut u8, size: *mut u32) -> i32 {
        (ZLIB.inflate_get_dictionary)(self, dictionary, size)
    }

    pub fn inflate_set_dictionary(self, dictionary: *const u8, size: u32) -> i32 {
        (ZLIB.inflate_set_dictionary)(self, dictionary, size)
    }

    pub fn inflate_ext(self: ZStream, next_in: *const u8, next_out: *mut u8,
                                      avail_in: u32, avail_out: u32,
                                      flush: i32) -> i32 {
        unsafe {
            (*self.0).next_in = next_in;
            (*self.0).avail_in = avail_in;
            (*self.0).next_out = next_out;
            (*self.0).avail_out = avail_out;
            let result = (ZLIB.inflate)(self.0, flush);

            (*self.0).next_in = null_mut();
            (*self.0).avail_in = 0;
            (*self.0).next_out = null_mut();
            (*self.0).avail_out = 0;
            result
        }
    }

    pub fn inflate_reset(self) -> i32 {
        (ZLIB.inflate_reset)(self)
    }

    pub fn inflate_end(self) -> i32 {
        (ZLIB.inflate_end)(self)
    }
}
