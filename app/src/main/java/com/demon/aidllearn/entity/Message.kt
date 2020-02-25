package com.demon.aidllearn.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * <p>
 * [类说明]
 * </p>
 *
 * @author zhaozeyang
 * @since 2020-02-20
 */
data class Message(var content: String?, var isSuccess: Boolean = false) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(content)
        writeInt((if (isSuccess) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Message> = object : Parcelable.Creator<Message> {
            override fun createFromParcel(source: Parcel): Message = Message(source)
            override fun newArray(size: Int): Array<Message?> = arrayOfNulls(size)
        }
    }
}