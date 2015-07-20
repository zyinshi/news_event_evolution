package edu.ucsd.xmlparser.entity;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class Word {
	@GraphId
	private Long id;
	
	@Indexed(indexType=IndexType.FULLTEXT, indexName = "wordtext")
	private String text;
	
	private int position = -1;
	
	@Indexed(indexType=IndexType.FULLTEXT, indexName = "pos")
	private String posTag;
	
	private String neTag;
	
	private Word() {	
	}
	
	public static Word newWord(String text, int position) {
		if(text == null) {
			throw new IllegalArgumentException("A sentence cannot be empty.");
		}
		
		if(position < 0) {
			throw new IllegalArgumentException("Word position can not be less than zero.");
		}
		
		Word newWord = new Word();
		newWord.text = text;
		newWord.position = position;
		
		return newWord;
	}
	
	public String getText() {
		return this.text;
	}
	
	public int getPosition() {
		return this.position;
	}
	
	public TextAndPosition getTextAndPosition() {
		return new TextAndPosition(getText(), getPosition());
	}
	
	public void setPosTag(String posTag) {
		if (posTag != null) {
			this.posTag = posTag;
		}
	}
	
	public void setNameEntityTag(String neTag) {
		if (neTag != null) {
			this.neTag = neTag;
		}
	}
		
	public Long getId() {
		return id;
	}

	public String getPosTag() {
		return posTag;
	}

	public String getNeTag() {
		return neTag;
	}

	
	
	@Override
	public String toString() {
		return "Word [id=" + id + ", text=" + text + ", position=" + position
				+ ", posTag=" + posTag + ", neTag=" + neTag + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + position;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Word other = (Word) obj;
		if (position != other.position)
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	public static class TextAndPosition {
		private String text;
		private int position;
		
		public TextAndPosition(String text, int position) {
			super();
			if(text == null) {
				throw new IllegalArgumentException("Text can not be null");
			}
			
			if(position < 0) {
				throw new IllegalArgumentException("Position can't be less than zero.");
			}
			this.text = text;
			this.position = position;
		}

		public String getText() {
			return text;
		}

		public int getPosition() {
			return position;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + position;
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TextAndPosition other = (TextAndPosition) obj;
			if (position != other.position)
				return false;
			if (text == null) {
				if (other.text != null)
					return false;
			} else if (!text.equals(other.text))
				return false;
			return true;
		}
		
	}
	
	public boolean neTagNotNullOrO() {
		return this.neTag != null && !this.neTag.equals("O");
	}
}